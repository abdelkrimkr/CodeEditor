package com.itsvks.code.core

data class TextPosition(val line: Int, val column: Int) : Comparable<TextPosition> {
    companion object {
        val Zero = TextPosition(0, 0)
    }

    override fun compareTo(other: TextPosition): Int {
        return when {
            line != other.line -> line - other.line
            else -> column - other.column
        }
    }

    operator fun rangeTo(other: TextPosition): TextPositionRange = TextPositionRange(this, other)
    //operator fun minus(other: TextPosition): TextPosition = TextPosition(line - other.line, column - other.column)
    //operator fun plus(other: TextPosition): TextPosition = TextPosition(line + other.line, column + other.column)
}

data class TextPositionRange(val start: TextPosition, val end: TextPosition) {
    val endInclusive: TextPosition
        get() = if (start <= end) end else start

    val startInclusive: TextPosition
        get() = if (start <= end) start else end

    private fun compareTextPositions(): Int {
        return if (start.line != end.line) {
            start.line - end.line
        } else {
            start.column - end.column
        }
    }

    fun normalize(): TextPositionRange {
        return if (compareTextPositions() <= 0) {
            this
        } else {
            TextPositionRange(this.end, this.start)
        }
    }
}
