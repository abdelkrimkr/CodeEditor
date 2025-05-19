package com.itsvks.code.core

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
 * @property length The total number of characters in the Rope.
 * @property lineCount The total number of lines in the Rope (number of newline characters + 1 if not empty, or 0 if empty).
 */
sealed class Rope {
    abstract val length: Int
    abstract val lineCount: Int // Represents the number of newline characters. The number of actual lines is lineCount + 1 (if length > 0).

    /**
     * Creates a deep copy of this Rope.
     *
     * @return A new Rope instance that is a copy of the current one.
     */
    abstract fun clone(): Rope

    /**
     * Represents a leaf node in the Rope tree, containing a segment of text.
     *
     * @param buffer The character array holding the text for this leaf.
     * @param start The starting index of this leaf's segment within the buffer.
     * @param length The number of characters in this leaf's segment.
     * @param lineCount The number of newline characters ('\n') within this leaf's segment.
     */
    data class Leaf(
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
         * Returns the string representation of this node by concatenating its children.
         * For debugging or full conversion. Can be expensive for large ropes.
         */
        override fun toString(): String {
            return toPlainString()
        }

        override fun clone(): Rope {
            return Node(left.clone(), right.clone())
        }
    }

    companion object {
        private const val MAX_LEAF_SIZE = 512

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
         * The file is read using UTF-8 encoding.
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

            // If the file was empty or an error occurred before any leaves were added.
            if (leaves.isEmpty()) return Leaf("")

            return buildTreeFromLeaves(leaves)
        }

        internal fun buildTreeFromLeaves(ropes: List<Rope>): Rope {
            if (ropes.isEmpty()) return Leaf("")
            if (ropes.size == 1) return ropes[0]

            val mid = ropes.size / 2

            val leftSubtree = buildTreeFromLeaves(ropes.subList(0, mid))
            val rightSubtree = buildTreeFromLeaves(ropes.subList(mid, ropes.size))

            return concatenate(leftSubtree, rightSubtree)
        }

        /**
         * Internal concatenation function that attempts to maintain some balance
         * by merging small adjacent leaves
         */
        internal fun concatenate(r1: Rope, r2: Rope): Rope {
            if (r1.length == 0) return r2
            if (r2.length == 0) return r1

            // Case 1: Both are leaves and their combined length is within MAX_LEAF_SIZE
            if (r1 is Leaf && r2 is Leaf && (r1.length + r2.length <= MAX_LEAF_SIZE)) {
                val newBuffer = CharArray(r1.length + r2.length)
                System.arraycopy(r1.buffer, r1.start, newBuffer, 0, r1.length)
                System.arraycopy(r2.buffer, r2.start, newBuffer, r1.length, r2.length)
                val newLines = r1.lineCount + r2.lineCount // Sum of line counts is correct here
                return Leaf(newBuffer, 0, newBuffer.size, newLines)
            }

            // Case 2: r1 is Node(L, R1_leaf) and r2 is R2_leaf. Try to merge R1_leaf and R2_leaf.
            // This helps in right-associative appends like (A+B)+C -> A+(B+C_merged) if B and C are small leaves.
            if (r1 is Node && r1.right is Leaf && r2 is Leaf) {
                if (r1.right.length + r2.length <= MAX_LEAF_SIZE) {
                    // The concatenate function will return a single merged Leaf here
                    val mergedRightLeaf = concatenate(r1.right, r2) as Leaf
                    return Node(r1.left, mergedRightLeaf)
                }
            }

            // Case 3: r1 is L1_leaf and r2 is Node(L2_leaf, R). Try to merge L1_leaf and L2_leaf.
            // This helps in left-associative prepends like A+(B+C) -> (A_merged+B)+C if A and B are small leaves.
            if (r1 is Leaf && r2 is Node && r2.left is Leaf) {
                if (r1.length + r2.left.length <= MAX_LEAF_SIZE) {
                    // The concatenate function will return a single merged Leaf here
                    val mergedLeftLeaf = concatenate(r1, r2.left) as Leaf
                    return Node(mergedLeftLeaf, r2.right)
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
                if (end <= leftLen) { // Slice is entirely within the left child
                    left.slice(start, end)
                } else if (start >= leftLen) { // Slice is entirely within the right child
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
                val left = this.slice(0, index)
                val right = this.slice(index, length)
                left to right
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
        return concatenate(leftPart, rightPart)
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
        val actualLineCount = if (length == 0) 0 else this.lineCount + (if (this.charAt(this.length - 1) == '\n') 0 else 1)
        require(lineIdx >= 0 && (if (actualLineCount == 0) lineIdx == 0 else lineIdx < actualLineCount)) {
            "Line index $lineIdx out of bounds (0..${actualLineCount - 1})"
        }
        if (length == 0 && lineIdx == 0) return Leaf("")

        var currentLineStartCharIdx = 0
        var currentLineNum = 0

        for (i in 0 until length) {
            if (currentLineNum == lineIdx) {
                var lineEndCharIdx = i
                while (lineEndCharIdx < length && charAt(lineEndCharIdx) != '\n') {
                    lineEndCharIdx++
                }

                return if (lineEndCharIdx < length && charAt(lineEndCharIdx) == '\n') {
                    slice(i, lineEndCharIdx + 1)
                } else { // Last line without a newline
                    slice(i, lineEndCharIdx)
                }
            }

            if (charAt(i) == '\n') {
                currentLineNum++
                currentLineStartCharIdx = i + 1
            }
        }
        // Should be caught by require, or if lineIdx is for the last line after all newlines
        if (currentLineNum == lineIdx && currentLineStartCharIdx < length) {
            return slice(currentLineStartCharIdx, length)
        }

        // If lineIdx is 0 for an empty rope, it's handled. Otherwise, this indicates an issue or out-of-bounds.
        error("Line $lineIdx not found, logic error or invalid index despite checks. Total lines: $actualLineCount, Rope length: $length")
    }


    /**
     * Retrieves a specific line or an empty Rope if the line index is out of bounds.
     *
     * @param lineIdx The 0-based index of the line.
     * @return A Rope containing the line, or an empty Rope.
     */
    fun lineOrEmpty(lineIdx: Int): Rope {
        val actualLineCount = if (length == 0) 0 else this.lineCount + (if (this.charAt(this.length - 1) == '\n') 0 else 1)
        return if (lineIdx >= 0 && (if (actualLineCount == 0) lineIdx == 0 else lineIdx < actualLineCount)) {
            line(lineIdx)
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
    fun lineToChar(lineIdx: Int): Int {
        val actualLineCount = if (length == 0) 0 else this.lineCount + (if (this.charAt(this.length - 1) == '\n') 0 else 1)
        require(lineIdx >= 0 && (if (actualLineCount == 0) lineIdx == 0 else lineIdx < actualLineCount)) {
            "lineToChar: Line index $lineIdx out of bounds (0..${actualLineCount - 1})"
        }
        if (length == 0 && lineIdx == 0) return 0

        if (lineIdx == 0) return 0 // First line always starts at index 0

        var currentLine = 0
        for (i in 0 until length) {
            if (charAt(i) == '\n') {
                currentLine++
                if (currentLine == lineIdx) {
                    return i + 1 // Start of next line
                }
            }
        }
        // This point should ideally not be reached if lineIdx is valid and not 0.
        // If lineIdx is for a line that doesn't exist (e.g., too high), the require should catch it.
        // If it's the last line and has no newline, it's handled by the loop not finding enough newlines.
        error("lineToChar: Logic error or invalid line index $lineIdx that bypassed checks.")
    }

    /**
     * Returns the line number (0-based) containing the character at the given index.
     *
     * This method is useful for converting character positions to line numbers.
     *
     * @param charIdx The 0-based character index.
     * @return The 0-based line number containing the character.
     * @throws IllegalArgumentException if `charIdx` is out of bounds.
     */
    fun charToLine(charIdx: Int): Int {
        require(charIdx in 0 .. length) { "Character index $charIdx out of bounds (0..$length)" }
        if (charIdx == length && length > 0 && charAt(length - 1) != '\n') return lineCount
        if (charIdx == length && length == 0) return 0
        if (charIdx == length) return lineCount

        var lines = 0
        for (i in 0 until charIdx) {
            if (charAt(i) == '\n') {
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
        for (i in 0 until length) {
            val c = charAt(i)
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
    fun lineColToChar(lineIdx: Int, colIdx: Int): Int {
        val lineStartChar = lineToChar(lineIdx)
        val lLength = lineLength(lineIdx)

        val effectiveLineLength = if (lLength > 0 && line(lineIdx).charAt(lLength - 1) == '\n') {
            lLength - 1
        } else {
            lLength
        }

        require(colIdx in 0 .. effectiveLineLength) {
            "Column index $colIdx out of bounds for line $lineIdx (length $effectiveLineLength)"
        }
        return lineStartChar + colIdx
    }


    /**
     * Converts the Rope to a plain String.
     * This function is primarily intended for debugging or when a full string representation is needed.
     * For very large Ropes, this operation can be expensive in terms of memory and time.
     *
     * @return The String representation of the Rope.
     */
    fun toPlainString(): String {
        if (length == 0) return ""
        val sb = StringBuilder(length)
        appendTo(sb)
        return sb.toString()
    }

    private fun appendTo(sb: StringBuilder) {
        when (this) {
            is Leaf -> sb.appendRange(this.buffer, this.start, this.start + this.length)
            is Node -> {
                left.appendTo(sb)
                right.appendTo(sb)
            }
        }
    }
}
