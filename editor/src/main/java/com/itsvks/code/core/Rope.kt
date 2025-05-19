package com.itsvks.code.core

import java.io.File

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
 * @property lineCount The total number of lines in the Rope (number of newline characters).
 */
sealed class Rope {
    abstract val length: Int
    abstract val lineCount: Int

    abstract fun clone(): Rope

    data class Leaf(
        val buffer: CharArray,
        val start: Int,
        override val length: Int,
        override val lineCount: Int,
    ) : Rope() {
        constructor(text: String) : this(text.toCharArray(), 0, text.length, text.count { it == '\n' })

        operator fun get(index: Int): Char {
            require(index in 0 until length)
            return buffer[start + index]
        }

        override fun toString(): String {
            return String(buffer, start, length)
        }

        override fun clone(): Rope {
            return Leaf(buffer.copyOf(), start, length, lineCount)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Leaf

            if (start != other.start) return false
            if (length != other.length) return false
            if (lineCount != other.lineCount) return false
            if (!buffer.contentEquals(other.buffer)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = start
            result = 31 * result + length
            result = 31 * result + lineCount
            result = 31 * result + buffer.contentHashCode()
            return result
        }
    }

    data class Node(val left: Rope, val right: Rope) : Rope() {
        override val length = left.length + right.length
        override val lineCount = left.lineCount + right.lineCount

        override fun toString(): String {
            return super.toPlainString()
        }

        override fun clone(): Rope {
            return Node(left.clone(), right.clone())
        }
    }

    companion object {
        private const val MAX_LEAF_SIZE = 512

        private fun balancedRope(nodes: List<Rope>): Rope {
            if (nodes.isEmpty()) return Leaf("")
            if (nodes.size == 1) return nodes[0]
            val mid = nodes.size / 2
            return Node(balancedRope(nodes.subList(0, mid)), balancedRope(nodes.subList(mid, nodes.size)))
        }

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
            if (length <= MAX_LEAF_SIZE) {
                var lines = 0
                for (i in start until start + length) {
                    if (buffer[i] == '\n') lines++
                }
                return Leaf(buffer, start, length, lines)
            }

            val mid = length / 2
            return Node(fromCharArray(buffer, start, mid), fromCharArray(buffer, start + mid, length - mid))
        }

        @JvmStatic
        fun fromFile(file: File): Rope {
            val leaves = mutableListOf<Rope>()
            file.inputStream().buffered().use { input ->
                val buffer = CharArray(MAX_LEAF_SIZE)
                val reader = input.reader()

                while (true) {
                    val readCount = reader.read(buffer, 0, MAX_LEAF_SIZE)
                    if (readCount <= 0) break
                    leaves.add(fromCharArray(buffer, 0, readCount))
                }
            }
            return balancedRope(leaves)
        }
    }

    operator fun plus(other: Rope) = balancedConcat(this, other)

    private fun collectLeavesTo(list: MutableList<Rope>) {
        when (this) {
            is Leaf -> list.add(this)
            is Node -> {
                left.collectLeavesTo(list)
                right.collectLeavesTo(list)
            }
        }
    }

    private fun balancedConcat(left: Rope, right: Rope): Rope {
        val leaves = mutableListOf<Rope>()
        left.collectLeavesTo(leaves)
        right.collectLeavesTo(leaves)
        return balancedRope(leaves)
    }

    fun charAt(index: Int): Char {
        require(index in 0 until length)
        return when (this) {
            is Leaf -> this[index]
            is Node -> {
                if (index < left.length) left.charAt(index)
                else right.charAt(index - left.length)
            }
        }
    }

    fun slice(start: Int, end: Int): Rope {
        require(start in 0 .. length) { "Slice start ($start) out of bounds (0..$length)" }
        require(end in start .. length) { "Slice end ($end) out of bounds ($start..$length)" }

        if (start == 0 && end == length) return this

        return when (this) {
            is Leaf -> fromCharArray(buffer, this.start + start, end - start)
            is Node -> {
                if (end <= left.length) left.slice(start, end)
                else if (start >= left.length) right.slice(start - left.length, end - left.length)
                else Node(left.slice(start, left.length), right.slice(0, end - left.length))
            }
        }
    }

    fun slice(range: IntRange) = slice(range.first, range.last + 1)

    fun split(index: Int): Pair<Rope, Rope> {
        require(index in 0 .. length) { "Split index ($index) out of bounds (0..$length)" }

        if (index == 0) return fromString("") to this
        if (index == length) return this to fromString("")

        return when (this) {
            is Leaf -> {
                val left = fromCharArray(buffer, start, index)
                val right = fromCharArray(buffer, start + index, length - index)
                left to right
            }

            is Node -> {
                if (index <= left.length) {
                    val (leftLeft, leftRight) = left.split(index)
                    leftLeft to Node(leftRight, right)
                } else {
                    val (rightLeft, rightRight) = right.split(index - left.length)
                    Node(left, rightLeft) to rightRight
                }
            }
        }
    }

    fun remove(start: Int, end: Int): Rope {
        require(start in 0 .. length && end in start .. length)
        val (left, temp) = split(start)
        val (_, right) = temp.split(end - start)
        return left + right
    }

    fun remove(range: IntRange) = remove(range.first, range.last + 1)

    fun insert(index: Int, text: String): Rope {
        require(index in 0 .. length)
        val (left, right) = split(index)
        return left + fromString(text) + right
    }

    /**
     * Retrieves a specific line from the Rope.
     *
     * Returns a new Rope instance representing the content of the specified line.
     * The returned Rope will include the newline character if the original line had one.
     * Line indices are 0-based.
     *
     * @param lineIdx The 0-based index of the line to retrieve.
     * @return A Rope containing the content of the specified line.
     * @throws IllegalArgumentException if `lineIdx` is out of bounds (less than 0 or greater than
     * or equal to `lineCount` if `lineCount` > 0, or `lineIdx` != 0 if `lineCount` == 0).
     *
     * @throws IllegalStateException if an invalid line index is encountered within a Leaf
     * node, which indicates an internal inconsistency.
     */
    fun line(lineIdx: Int): Rope {
        require(lineIdx in 0 .. lineCount) { "Line index $lineIdx out of bounds (0..$lineCount)" }

        return when (this) {
            is Leaf -> {
                var currentLine = 0

                for (i in 0 until length) {
                    if (currentLine == lineIdx) {
                        for (j in i until length) {
                            if (buffer[start + j] == '\n') {
                                return fromCharArray(buffer, start + i, j - i + 1)//+1 for new line character
                            }
                        }
                        return fromCharArray(buffer, start + i, length - i)
                    }

                    if (buffer[start + i] == '\n') currentLine++
                }

                error("Invalid line index in Leaf")
            }

            is Node -> {
                if (lineIdx < left.lineCount) left.line(lineIdx)
                else right.line(lineIdx - left.lineCount)
            }
        }
    }

    /**
     * Returns the character index of the start of the given line.
     *
     * @param lineIdx The 0-based index of the line.
     * @return The character index of the start of the specified line.
     * @throws IllegalArgumentException if `lineIdx` is out of bounds (less than 0 or greater than or equal to `lineCount`).
     * @throws IllegalStateException if an invalid line index is encountered within a Leaf node, which indicates an internal inconsistency.
     */
    fun lineToChar(lineIdx: Int): Int {
        require(lineIdx in 0 .. lineCount) { "Line index $lineIdx out of bounds (0..$lineCount)" }

        return when (this) {
            is Leaf -> {
                var currentLine = 0
                for (i in 0 until length) {
                    if (currentLine == lineIdx) return i
                    if (buffer[start + i] == '\n') currentLine++
                }
                error("Invalid line index in Leaf")
            }

            is Node -> {
                if (lineIdx < left.lineCount) left.lineToChar(lineIdx)
                else left.length + right.lineToChar(lineIdx - left.lineCount)
            }
        }
    }

    /**
     * Returns the line number (0-based) containing the character at the given index.
     *
     * @param charIdx The 0-based character index.
     * @return The 0-based line number containing the character.
     * @throws IllegalArgumentException if `charIdx` is out of bounds (less than 0 or greater than or equal to `length`).
     * @throws IllegalStateException if an invalid character index is encountered within a Leaf node, which indicates an internal inconsistency.
     */
    fun charToLine(charIdx: Int): Int {
        require(charIdx in 0 .. length) { "Character index $charIdx out of bounds (0..$length)" }

        return when (this) {
            is Leaf -> {
                var currentLine = 0
                for (i in 0 until length) {
                    if (i == charIdx) return currentLine
                    if (buffer[start + i] == '\n') currentLine++
                }
                error("Invalid character index in Leaf")
            }

            is Node -> {
                if (charIdx < left.length) left.charToLine(charIdx)
                else left.lineCount + right.charToLine(charIdx - left.length)
            }
        }
    }

    /**
     * Calculates the length of a specific line in the Rope, including the newline character if present.
     *
     * Line indices are 0-based.
     *
     * @param lineIdx The 0-based index of the line whose length is to be determined.
     * @return The length of the specified line, including the newline character.
     * @throws IllegalArgumentException if `lineIdx` is out of bounds (less than 0 or greater than or equal to `lineCount`).
     */
    fun lineLength(lineIdx: Int): Int {
        require(lineIdx in 0 .. lineCount) { "Line index $lineIdx out of bounds (0..$lineCount)" }

        return when (this) {
            is Leaf -> {
                var currentLine = 0
                for (i in 0 until length) {
                    if (currentLine == lineIdx) {
                        for (j in i until length) {
                            if (buffer[start + j] == '\n') return j - i + 1
                        }
                        return length - i
                    }
                    if (buffer[start + i] == '\n') currentLine++
                }
                error("Invalid line index in Leaf")
            }

            is Node -> {
                if (lineIdx < left.lineCount) left.lineLength(lineIdx)
                else right.lineLength(lineIdx - left.lineCount)
            }
        }
    }

    fun maxLineLength(): Int {
        var maxLength = 0
        var currentLength = 0

        for (i in 0 until length) {
            val c = charAt(i)
            if (c == '\n') {
                if (currentLength > maxLength) {
                    maxLength = currentLength
                }
                currentLength = 0
            } else {
                currentLength++
            }
        }

        // Handle the last line (if no trailing newline)
        if (currentLength > maxLength) {
            maxLength = currentLength
        }

        return maxLength
    }

    fun lineLengthUpTo(lineIdx: Int, colIdx: Int): Int {
        val lineStart = lineToChar(lineIdx)
        return colIdx + lineStart
    }

    /**
     * Converts the Rope to a plain String.
     *
     * This function is primarily intended for debugging purposes.
     * For very large Ropes, this operation can be expensive in terms of memory and time
     * as it requires concatenating all the leaf nodes.
     *
     * @return The String representation of the Rope.
     */
    fun toPlainString(): String {
        return buildString {
            when (this@Rope) {
                is Leaf -> append(this@Rope.toString())
                is Node -> {
                    append(left.toPlainString())
                    append(right.toPlainString())
                }
            }
        }
    }
}
