package com.itsvks.code.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.IOException

/**
 * Represents an immutable sequence of characters, optimized for efficient editing operations
 * on large texts, such as insertion, deletion, and concatenation.
 *
 * A Rope is a tree-like data structure where leaves contain character arrays (segments of the text)
 * and internal nodes represent the concatenation of their children. This structure allows
 * many operations to be performed in logarithmic time with respect to the length of the string,
 * avoiding the need to copy the entire string as is common with traditional string implementations.
 *
 * Ropes are particularly useful in text editors, version control systems, and other applications
 * that deal with large, frequently modified text documents.
 *
 * Ropes are immutable; all modification operations return a new Rope instance, leaving the
 * original unchanged.
 *
 * **Note**: This is simple implementation of Rope data structure. But it can still handle 1 million lines of text.
 *           I will improve it in future.
 *
 * @property length The total number of characters in the Rope.
 * @property lineCount The total number of lines in the Rope (number of newline characters).
 * The number of actual text lines is typically lineCount + 1.
 */
sealed class Rope : Iterable<Char> {
    abstract val length: Int
    abstract val lineCount: Int // Represents the number of newline characters.

    /**
     * The total number of text lines in the Rope.
     * This is typically `lineCount` (number of newline characters) + 1.
     * For an empty Rope, it's considered to have 1 line (an empty line).
     */
    val totalLines: Int get() = lineCount + 1

    /**
     * Creates a deep copy of this Rope.
     *
     * @return A new Rope instance that is a copy of the current one.
     */
    abstract fun clone(): Rope

    /**
     * Represents a leaf node in the Rope tree, containing a segment of text.
     *
     * The primary constructor is internal to encourage use of factory methods that ensure
     * proper copying and handling of character arrays for immutability.
     *
     * @param buffer The character array holding the text for this leaf. This array should be exclusive to this Leaf instance.
     * @param start The starting index of this leaf's segment within the buffer (typically 0 for buffers owned by this leaf).
     * @param length The number of characters in this leaf's segment.
     * @param lineCount The number of newline characters ('\n') within this leaf's segment.
     */
    data class Leaf internal constructor(
        val buffer: CharArray,
        val start: Int,
        override val length: Int,
        override val lineCount: Int,
    ) : Rope() {
        constructor(text: String) : this(text.toCharArray(), 0, text.length, text.count { it == '\n' })

        /**
         * Gets the character at the specified relative index within this leaf.
         *
         * @param index The 0-based index within this leaf's segment.
         * @return The character at the specified index.
         * @throws IllegalArgumentException if the index is out of bounds.
         */
        operator fun get(index: Int): Char {
            require(index in 0 until length) { "Leaf index out of bounds: $index, length: $length" }
            return buffer[start + index]
        }

        /**
         * Returns the string representation of this leaf's segment.
         */
        override fun toString(): String {
            return String(buffer, start, length)
        }

        override fun clone(): Rope {
            val newBuffer = CharArray(length)
            System.arraycopy(buffer, this.start, newBuffer, 0, length)
            return Leaf(newBuffer, 0, length, lineCount) // New start is 0 as it's a fresh, minimal buffer
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Leaf) return false

            if (length != other.length) return false
            if (lineCount != other.lineCount) return false

            for (i in 0 until length) {
                if (buffer[start + i] != other.buffer[other.start + i]) return false
            }
            return true
        }

        override fun hashCode(): Int {
            var result = length
            result = 31 * result + lineCount
            for (i in 0 until length) {
                result = 31 * result + buffer[start + i].hashCode()
            }
            return result
        }
    }

    /**
     * Represents an internal node in the Rope tree, concatenating two child Ropes.
     *
     * @param left The left child Rope.
     * @param right The right child Rope.
     */
    data class Node(val left: Rope, val right: Rope) : Rope() {
        override val length = left.length + right.length
        override val lineCount = left.lineCount + right.lineCount

        /**
         * Returns a structural string representation of this node for debugging.
         * Use [toPlainString] for the full concatenated string content.
         */
        override fun toString(): String { // Changed from toPlainString()
            return "Node(len=$length, lines=$lineCount, L_len=${left.length}, R_len=${right.length})"
        }

        override fun clone(): Rope {
            return Node(left.clone(), right.clone())
        }
    }

    private fun writeRope(writer: BufferedWriter) {
        when (this) {
            is Leaf -> writer.write(buffer, start, length)
            is Node -> {
                left.writeRope(writer)
                right.writeRope(writer)
            }
        }
    }

    /**
     * Saves this rope to a file in a memory-efficient and high-performance manner.
     *
     * It works well for large files (even GBs), thanks to [BufferedWriter].
     *
     * @param file The destination [File] where the rope’s content should be saved.
     */
    suspend fun saveToFile(file: File) = withContext(Dispatchers.IO) {
        file.bufferedWriter(Charsets.UTF_8).use(::writeRope)
    }

    companion object {
        private const val MAX_LEAF_SIZE = 512
        private const val MIN_LEAF_SIZE = MAX_LEAF_SIZE / 4

        /**
         * Creates a Rope from a String.
         *
         * This is a common way to initialize a Rope from existing string data.
         *
         * @param str The String to convert into a Rope.
         * @return A Rope instance representing the input string.
         */
        @JvmStatic
        fun fromString(str: String): Rope {
            if (str.isEmpty()) return Leaf("")
            if (str.length <= MAX_LEAF_SIZE) return Leaf(str)
            val mid = str.length / 2
            return Node(fromString(str.substring(0, mid)), fromString(str.substring(mid)))
        }

        /**
         * Creates a Rope from a character array.
         *
         * This method is more efficient than [fromString] when the text is already available
         * as a character array, as it avoids the overhead of string creation and substring operations.
         *
         * @param buffer The character array containing the text.
         * @param start The starting index of the segment in the buffer (inclusive).
         * @param length The length of the segment.
         * @return A Rope representing the specified segment of the character array.
         */
        @JvmStatic
        fun fromCharArray(buffer: CharArray, start: Int, length: Int): Rope {
            if (length == 0) return Leaf("")
            require(start >= 0 && length >= 0 && start + length <= buffer.size) { "Invalid char array segment" }

            if (length <= MAX_LEAF_SIZE) {
                var lines = 0
                for (i in start until start + length) {
                    if (buffer[i] == '\n') lines++
                }

                val leafBuffer = buffer.copyOfRange(start, start + length)
                return Leaf(leafBuffer, 0, length, lines)
            }

            val mid = length / 2
            return Node(
                fromCharArray(buffer, start, mid),
                fromCharArray(buffer, start + mid, length - mid)
            )
        }

        /**
         * Creates a Rope from the content of a File.
         *
         * The file is read using UTF-8 encoding. This method is optimized for large files by reading in chunks.
         *
         * @param file The [File] to read content from.
         * @return A [Rope] instance representing the content of the file.
         * @throws java.io.IOException if an error occurs during file reading
         */
        @JvmStatic
        fun fromFile(file: File): Rope {
            val leaves = mutableListOf<Rope>()
            val bufferSize = MAX_LEAF_SIZE
            val charBuffer = CharArray(bufferSize)

            try {
                file.bufferedReader(Charsets.UTF_8).use { reader ->
                    var charsRead: Int
                    while (reader.read(charBuffer, 0, bufferSize).also { charsRead = it } != -1) {
                        if (charsRead > 0) {
                            val currentChunkData = charBuffer.copyOfRange(0, charsRead)

                            var linesInChunk = 0
                            for (i in 0 until charsRead) {
                                if (currentChunkData[i] == '\n') {
                                    linesInChunk++
                                }
                            }

                            leaves.add(Leaf(currentChunkData, 0, charsRead, linesInChunk))
                        }
                    }
                }
            } catch (e: IOException) {
                System.err.println("Error reading file ${file.path}: ${e.message}")
                throw e
            }

            if (leaves.isEmpty()) return Leaf("")
            return buildTreeFromLeaves(leaves)
        }

        private fun buildTreeFromLeaves(ropes: List<Rope>): Rope {
            if (ropes.isEmpty()) return Leaf("")
            if (ropes.size == 1) return ropes[0]

            val mid = ropes.size / 2
            val leftSubtree = buildTreeFromLeaves(ropes.subList(0, mid))
            val rightSubtree = buildTreeFromLeaves(ropes.subList(mid, ropes.size))
            return concatenate(leftSubtree, rightSubtree)
        }

        internal fun concatenate(r1: Rope, r2: Rope): Rope {
            if (r1.length == 0) return r2
            if (r2.length == 0) return r1

            // Case 1: Both are leaves. Try to merge if they fit MAX_LEAF_SIZE,
            // or if one is very small (below MIN_LEAF_SIZE) and they can be combined.
            if (r1 is Leaf && r2 is Leaf) {
                if (r1.length + r2.length <= MAX_LEAF_SIZE ||
                    ((r1.length < MIN_LEAF_SIZE || r2.length < MIN_LEAF_SIZE)
                            && r1.length + r2.length <= MAX_LEAF_SIZE + MIN_LEAF_SIZE / 2) // Allow slight oversize if merging tiny leaves
                ) {
                    if (r1.length + r2.length <= MAX_LEAF_SIZE * 1.25) { // Put a hard cap on how much oversize we allow
                        val newBuffer = CharArray(r1.length + r2.length)
                        System.arraycopy(r1.buffer, r1.start, newBuffer, 0, r1.length)
                        System.arraycopy(r2.buffer, r2.start, newBuffer, r1.length, r2.length)
                        val newLines = r1.lineCount + r2.lineCount
                        return Leaf(newBuffer, 0, newBuffer.size, newLines)
                    }
                }
            }

            // Case 2: r1 is Node(L, R1_leaf) and r2 is R2_leaf. Try to merge R1_leaf and R2_leaf.
            if (r1 is Node && r1.right is Leaf && r2 is Leaf) {
                val r1Right = r1.right as Leaf
                // Try to merge if combined is within MAX_LEAF_SIZE or if one is tiny
                if (r1Right.length + r2.length <= MAX_LEAF_SIZE ||
                    ((r1Right.length < MIN_LEAF_SIZE || r2.length < MIN_LEAF_SIZE)
                            && r1Right.length + r2.length <= MAX_LEAF_SIZE + MIN_LEAF_SIZE / 2)
                ) {
                    if (r1Right.length + r2.length <= MAX_LEAF_SIZE * 1.25) {
                        val mergedRight = concatenate(r1Right, r2)
                        return Node(r1.left, mergedRight)
                    }
                }
            }

            // Case 3: r1 is L1_leaf and r2 is Node(L2_leaf, R). Try to merge L1_leaf and L2_leaf.
            if (r1 is Leaf && r2 is Node && r2.left is Leaf) {
                val r2Left = r2.left as Leaf
                if (r1.length + r2Left.length <= MAX_LEAF_SIZE ||
                    ((r1.length < MIN_LEAF_SIZE || r2Left.length < MIN_LEAF_SIZE) && r1.length + r2Left.length <= MAX_LEAF_SIZE + MIN_LEAF_SIZE / 2)
                ) {
                    if (r1.length + r2Left.length <= MAX_LEAF_SIZE * 1.25) {
                        val mergedLeft = concatenate(r1, r2Left)
                        return Node(mergedLeft, r2.right)
                    }
                }
            }

            return Node(r1, r2)
        }
    }

    /**
     * Concatenates this Rope with another Rope.
     *
     * @param other The Rope to append.
     * @return A new Rope representing the concatenation.
     */
    operator fun plus(other: Rope): Rope = concatenate(this, other)

    /**
     * Retrieves the character at the specified absolute index in the Rope.
     * This operation is efficient, typically O(log N) where N is the Rope's length.
     *
     * @param index The 0-based index of the character.
     * @return The character at the specified index.
     * @throws IllegalArgumentException if the index is out of bounds.
     */
    fun charAt(index: Int): Char {
        require(index in 0 until length) { "charAt index ($index) out of bounds (0..${length - 1})" }
        return when (this) {
            is Leaf -> this[index]
            is Node -> {
                if (index < left.length) left.charAt(index)
                else right.charAt(index - left.length)
            }
        }
    }

    /**
     * Creates a new Rope representing a slice (substring) of this Rope.
     * Sliced portions might result in leaves smaller than MIN_LEAF_SIZE;
     * further operations or explicit rebalancing might be needed to normalize them.
     *
     * @param start The starting index of the slice (inclusive).
     * @param end The ending index of the slice (exclusive).
     * @return A new Rope representing the specified slice.
     * @throws IllegalArgumentException if start or end are out of bounds or end < start.
     */
    fun slice(start: Int, end: Int): Rope {
        require(start in 0 .. length) { "Slice start ($start) out of bounds (0..$length)" }
        require(end in start .. length) { "Slice end ($end) out of bounds ($start..$length)" }

        if (start == end) return Leaf("")
        if (start == 0 && end == length) return this

        return when (this) {
            is Leaf -> {
                val sliceLength = end - start
                val newBuffer = CharArray(sliceLength)
                System.arraycopy(this.buffer, this.start + start, newBuffer, 0, sliceLength)
                var newLines = 0
                for (char_idx in 0 until sliceLength) {
                    if (newBuffer[char_idx] == '\n') newLines++
                }
                Leaf(newBuffer, 0, sliceLength, newLines)
            }

            is Node -> {
                val leftLen = left.length
                if (end <= leftLen) {
                    left.slice(start, end)
                } else if (start >= leftLen) {
                    right.slice(start - leftLen, end - leftLen)
                } else {
                    concatenate(
                        left.slice(start, leftLen),
                        right.slice(0, end - leftLen)
                    )
                }
            }
        }
    }

    /**
     * Creates a new Rope representing a slice (substring) of this Rope using an IntRange.
     * The range is inclusive.
     *
     * @param range The IntRange specifying the slice (e.g., `0..5` for the first 6 characters).
     * @return A new Rope representing the specified slice.
     */
    fun slice(range: IntRange): Rope = slice(range.first, range.last + 1)

    /**
     * Splits the Rope into two Ropes at the specified index.
     * Resulting parts might be smaller than MIN_LEAF_SIZE.
     *
     * @param index The index at which to split. The character at this index will be the first
     * character of the second (right) Rope.
     * @return A Pair of Ropes: the left part and the right part.
     * @throws IllegalArgumentException if the index is out of bounds.
     */
    fun split(index: Int): Pair<Rope, Rope> {
        require(index in 0 .. length) { "Split index ($index) out of bounds (0..$length)" }

        if (index == 0) return Leaf("") to this
        if (index == length) return this to Leaf("")

        return when (this) {
            is Leaf -> {
                val leftRope = this.slice(0, index)
                val rightRope = this.slice(index, length)
                leftRope to rightRope
            }

            is Node -> {
                val leftLen = left.length
                if (index < leftLen) {
                    val (leftLeft, leftRight) = left.split(index)
                    leftLeft to concatenate(leftRight, right)
                } else if (index > leftLen) {
                    val (rightLeft, rightRight) = right.split(index - leftLen)
                    concatenate(left, rightLeft) to rightRight
                } else { // index == left.length
                    left to right
                }
            }
        }
    }

    /**
     * Removes a segment of text from the Rope.
     *
     * @param start The starting index of the segment to remove (inclusive).
     * @param end The ending index of the segment to remove (exclusive).
     * @return A new Rope with the segment removed.
     */
    fun remove(start: Int, end: Int): Rope {
        require(start in 0 .. length) { "Remove start ($start) out of bounds (0..$length)" }
        require(end in start .. length) { "Remove end ($end) out of bounds ($start..$length)" }
        if (start == end) return this

        val (leftPart, temp) = split(start)
        val (_, rightPart) = temp.split(end - start)
        return concatenate(leftPart, rightPart) // Use companion object's concatenate
    }

    /**
     * Removes a segment of text from the Rope using an IntRange.
     * The range is inclusive.
     *
     * @param range The IntRange specifying the segment to remove.
     * @return A new Rope with the segment removed.
     */
    fun remove(range: IntRange): Rope = remove(range.first, range.last + 1)

    /**
     * Inserts a string into the Rope at the specified index.
     *
     * @param index The index at which to insert the text.
     * @param text The String to insert.
     * @return A new Rope with the text inserted.
     */
    fun insert(index: Int, text: String): Rope {
        require(index in 0 .. length) { "Insert index ($index) out of bounds (0..$length)" }
        if (text.isEmpty()) return this

        val (leftPart, rightPart) = split(index)
        val textRope = fromString(text)
        return concatenate(concatenate(leftPart, textRope), rightPart)
    }

    fun isEmpty(): Boolean = length == 0

    /**
     * Retrieves a specific line from the Rope.
     *
     * A line is defined as text ending with a newline character ('\n') or the end of the Rope.
     * The returned Rope will include the newline character if present.
     *
     * Line indices are 0-based.
     *
     * @param lineIdx The 0-based index of the line to retrieve.
     * @return A Rope containing the content of the specified line.
     * @throws IllegalArgumentException if `lineIdx` is out of bounds.
     */
    fun line(lineIdx: Int): Rope {
        val actualTextLines = if (length == 0 && lineCount == 0) 1 else lineCount + 1
        require(lineIdx in 0 ..< actualTextLines) {
            "Line index $lineIdx out of bounds for $actualTextLines text lines (0..${actualTextLines - 1})"
        }
        if (length == 0 && lineIdx == 0) return Leaf("")

        val currentLineStartCharIdx: Int
        var numNewlinesEncountered = 0

        if (lineIdx == 0) { // first line
            currentLineStartCharIdx = 0
        } else {
            // find start of target line
            var charSearchIdx = 0
            while (charSearchIdx < length && numNewlinesEncountered < lineIdx) {
                if (charAt(charSearchIdx) == '\n') {
                    numNewlinesEncountered++
                }
                charSearchIdx++
            }

            if (numNewlinesEncountered < lineIdx) {
                error("Line $lineIdx not found, despite passing bounds check (A).")
            }

            currentLineStartCharIdx = charSearchIdx
        }

        var lineEndCharIdx = currentLineStartCharIdx
        while (lineEndCharIdx < length && charAt(lineEndCharIdx) != '\n') {
            lineEndCharIdx++
        }

        return if (lineEndCharIdx < length && charAt(lineEndCharIdx) == '\n') {
            slice(currentLineStartCharIdx, lineEndCharIdx + 1) // include newline
        } else { // last line or line without a newline (should just be last line)
            slice(currentLineStartCharIdx, lineEndCharIdx)
        }
    }


    /**
     * Retrieves a specific line or an empty Rope if the line index is out of bounds.
     *
     * @param lineIdx The 0-based index of the line.
     * @return A Rope containing the line, or an empty Rope.
     */
    fun lineOrEmpty(lineIdx: Int): Rope {
        val actualTextLines = if (length == 0 && lineCount == 0) 1 else lineCount + 1
        return if (lineIdx in 0 ..< actualTextLines) {
            try {
                line(lineIdx)
            } catch (e: IllegalArgumentException) {
                Leaf("")
            }
        } else {
            Leaf("")
        }
    }


    /**
     * Returns the character index of the start of the given line.
     *
     * This method is useful for converting line numbers to character positions.
     * Line indices are 0-based.
     *
     * @param lineIdx The 0-based index of the line.
     * @return The character index (0-based) of the start of the specified line.
     * @throws IllegalArgumentException if `lineIdx` is out of bounds.
     */
    fun getLineStartCharIndex(lineIdx: Int): Int {
        val actualTextLines = if (length == 0 && lineCount == 0) 1 else lineCount + 1
        require(lineIdx in 0 ..< actualTextLines) {
            "getLineStartCharIndex: Line index $lineIdx out of bounds for $actualTextLines text lines (0..${actualTextLines - 1})"
        }

        if (lineIdx == 0) return 0

        var numNewlinesToFind = lineIdx
        for (i in 0 until length) {
            if (charAt(i) == '\n') {
                numNewlinesToFind--
                if (numNewlinesToFind == 0) {
                    return i + 1 // Character after the (lineIdx-1)-th newline
                }
            }
        }
        // This should not be reached if lineIdx is valid and > 0,
        // as it implies not enough newlines were found for the requested line index.
        // This can happen if lineIdx == lineCount + 1 (i.e. actualTextLines) which is out of bounds for 0-indexed lineIdx
        error("lineToChar: Logic error or invalid line index $lineIdx that bypassed checks for line starting char.")
    }

    /**
     * Returns the line number (0-based) containing the character at the given index.
     *
     * This method is useful for converting character positions to line numbers.
     *
     * @param charIdx The 0-based character index. `length` is a valid index representing cursor after last char.
     * @return The 0-based line number containing the character.
     * @throws IllegalArgumentException if `charIdx` is out of bounds (less than 0 or greater than `length`).
     */
    fun lineAt(charIdx: Int): Int {
        require(charIdx in 0 .. length) { "Character index $charIdx out of bounds (0..$length)" }

        if (length == 0) return 0

        var lines = 0
        for (i in 0 until charIdx) {
            if (i < length && charAt(i) == '\n') {
                lines++
            }
        }
        return lines
    }

    /**
     * Calculates the length of a specific line in the Rope, including the newline character if present.
     *
     * Line indices are 0-based.
     *
     * @param lineIdx The 0-based index of the line whose length is to be determined.
     * @return The length of the specified line, including the newline character if applicable.
     * @throws IllegalArgumentException if `lineIdx` is out of bounds.
     */
    fun lineLength(lineIdx: Int): Int {
        val lineRope = line(lineIdx)
        return lineRope.length
    }

    /**
     * Finds the maximum length of any single line in the Rope.
     * Line length does not include the newline character itself for this calculation.
     *
     * @return The maximum line length.
     */
    fun maxLineLength(): Int {
        if (length == 0) return 0
        var maxLength = 0
        var currentLength = 0
        for (c in this) {
            if (c == '\n') {
                maxLength = maxOf(maxLength, currentLength)
                currentLength = 0
            } else {
                currentLength++
            }
        }
        maxLength = maxOf(maxLength, currentLength)
        return maxLength
    }

    /**
     * Calculates the absolute character index corresponding to a given line and column.
     *
     * @param lineIdx The 0-based line index.
     * @param colIdx The 0-based column index within that line.
     * @return The absolute character index.
     * @throws IllegalArgumentException if lineIdx or colIdx are out of bounds.
     */
    fun lineColumnToCharIndex(lineIdx: Int, colIdx: Int): Int {
        val lineStartChar = getLineStartCharIndex(lineIdx)
        val currentLine = line(lineIdx)
        val lLength = currentLine.length

        val effectiveContentLength = if (lLength > 0 && currentLine.charAt(lLength - 1) == '\n') {
            lLength - 1
        } else {
            lLength
        }

        require(colIdx in 0 .. effectiveContentLength) { // colIdx can be after last char on line
            "Column index $colIdx out of bounds for line $lineIdx (content length $effectiveContentLength)"
        }
        return lineStartChar + colIdx
    }

    /** Iterates over lines in [start, end) */
    inline fun forLines(start: Int, end: Int, action: (Rope) -> Unit) {
        val last = end.coerceAtMost(totalLines)
        for (i in start until last) {
            action(line(i))
        }
    }

    /** Iterates over lines with index in [start, end) */
    inline fun forLinesIndexed(start: Int, end: Int, action: (Int, Rope) -> Unit) {
        val last = end.coerceAtMost(totalLines)
        for (i in start until last) {
            action(i, line(i))
        }
    }

    /** Iterates over lines in a range */
    inline fun forLines(range: IntRange, action: (Rope) -> Unit) = forLines(range.first, range.last + 1, action)

    /** Iterates over lines in a range with indices */
    inline fun forLinesIndexed(range: IntRange, action: (Int, Rope) -> Unit) = forLinesIndexed(range.first, range.last + 1, action)

    /** Iterates over all lines */
    inline fun forEachLine(action: (Rope) -> Unit) = forLines(0, totalLines, action)

    /** Iterates over all lines with index */
    inline fun forEachLineIndexed(action: (Int, Rope) -> Unit) = forLinesIndexed(0, totalLines, action)

    /** Returns all lines as a list of ropes */
    fun lines(): List<Rope> = buildList(totalLines) { forEachLine { add(it) } }

    /**
     * Converts the Rope to a plain String.
     * This function is primarily intended for debugging or when a full string representation is needed.
     * For very large Ropes, this operation can be expensive in terms of memory and time.
     *
     * @return The String representation of the Rope.
     */
    fun toPlainString(): String {
        if (length == 0) return ""
        val sb = StringBuilder(length.coerceAtLeast(16)) // Ensure minimum capacity
        appendTo(sb)
        return sb.toString()
    }

    /**
     * Internal helper to recursively append content to a StringBuilder.
     * This is efficient due to direct char array appends.
     */
    private fun appendTo(sb: StringBuilder) {
        when (this) {
            is Leaf -> sb.appendRange(this.buffer, this.start, this.start + this.length)
            is Node -> {
                left.appendTo(sb)
                right.appendTo(sb)
            }
        }
    }

    /**
     * Returns an iterator over the characters in this Rope.
     */
    final override fun iterator(): CharIterator = RopeIterator(this)

    private class RopeIterator(rope: Rope) : CharIterator() {
        private val stack = ArrayDeque<Rope>()
        private var currentLeaf: Leaf? = null
        private var indexInLeaf = 0

        init {
            var r = rope
            while (r is Node) {
                if (r.right.length > 0) stack.addLast(r.right)
                r = r.left
            }
            if (r is Leaf && r.length > 0) {
                currentLeaf = r
            } else { // initial rope is empty or only contains empty leaves
                advanceToNextLeaf()
            }
        }

        private fun advanceToNextLeaf() {
            currentLeaf = null
            indexInLeaf = 0
            while (stack.isNotEmpty()) {
                var nextRope = stack.removeLast()
                while (nextRope is Node) {
                    if (nextRope.right.length > 0) stack.addLast(nextRope.right)
                    nextRope = nextRope.left
                }
                if (nextRope is Leaf && nextRope.length > 0) {
                    currentLeaf = nextRope
                    return
                }
            }
        }

        override fun hasNext(): Boolean {
            return currentLeaf != null && indexInLeaf < currentLeaf!!.length
        }

        override fun nextChar(): Char {
            val leaf = currentLeaf ?: throw NoSuchElementException()
            if (indexInLeaf >= leaf.length) throw NoSuchElementException()

            val char = leaf.buffer[leaf.start + indexInLeaf]
            indexInLeaf++

            if (indexInLeaf >= leaf.length) {
                advanceToNextLeaf()
            }
            return char
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rope) return false
        if (length != other.length) return false
        if (lineCount != other.lineCount) return false
        if (hashCode() != other.hashCode()) return false

        val iter1 = this.iterator()
        val iter2 = other.iterator()
        while (iter1.hasNext() && iter2.hasNext()) {
            if (iter1.nextChar() != iter2.nextChar()) return false
        }

        return !iter1.hasNext() && !iter2.hasNext()
    }

    override fun hashCode(): Int {
        var h = 1
        for (char in this) {
            h = 31 * h + char.hashCode()
        }
        return h
    }

    /**
     * Finds the first occurrence of a given pattern string within the Rope,
     * starting from a specified index.
     *
     * @param pattern The string pattern to search for.
     * @param startIndex The 0-based index from which to start the search. Defaults to 0.
     * @return The 0-based index of the first occurrence of the pattern, or -1 if not found.
     */
    fun indexOf(pattern: String, startIndex: Int = 0): Int {
        if (pattern.isEmpty()) return startIndex.coerceIn(0, this.length)
        val patternLen = pattern.length
        if (patternLen == 0) return startIndex.coerceIn(0, this.length)

        if (startIndex < 0 || patternLen > this.length - startIndex) {
            return -1
        }

        val ropeLen = this.length
        if (patternLen > ropeLen) return -1

        val patternChars = pattern.toCharArray()

        var i = startIndex
        while (i <= ropeLen - patternLen) {
            var match = true
            for (j in 0 until patternLen) {
                if (this.charAt(i + j) != patternChars[j]) {
                    match = false
                    break
                }
            }

            if (match) return i
            i++
        }
        return -1
    }


    /**
     * Finds the last occurrence of a given pattern string within the Rope,
     * searching backwards from a specified index.
     *
     * @param pattern The string pattern to search for.
     * @param startIndex The 0-based index from which to start the search backwards.
     * Defaults to the last possible start index for the pattern.
     * @return The 0-based index of the first character of the last occurrence, or -1 if not found.
     */
    fun lastIndexOf(pattern: String, startIndex: Int = this.length - pattern.length): Int {
        if (pattern.isEmpty()) return startIndex.coerceIn(0, this.length)
        val patternLen = pattern.length
        if (patternLen == 0) return startIndex.coerceIn(0, this.length)

        if (patternLen > this.length) return -1

        val effectiveStartIndex = startIndex.coerceIn(0, this.length - patternLen)

        for (i in effectiveStartIndex downTo 0) {
            var match = true
            for (j in 0 until patternLen) {
                if (this.charAt(i + j) != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }

    operator fun contains(pattern: String): Boolean = indexOf(pattern) != -1
}
