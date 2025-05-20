package com.itsvks.code.core

data class CursorPosition(val line: Int, val column: Int) : Comparable<CursorPosition> {
    companion object {
        val Zero = CursorPosition(0, 0)
    }

    override fun compareTo(other: CursorPosition): Int {
        return when {
            line != other.line -> line - other.line
            else -> column - other.column
        }
    }

    operator fun rangeTo(other: CursorPosition): CursorRange = CursorRange(this, other)
    //operator fun minus(other: CursorPosition): CursorPosition = CursorPosition(line - other.line, column - other.column)
    //operator fun plus(other: CursorPosition): CursorPosition = CursorPosition(line + other.line, column + other.column)
}

data class CursorRange(val start: CursorPosition, val end: CursorPosition) {
    val endInclusive: CursorPosition
        get() = if (start <= end) end else start

    val startInclusive: CursorPosition
        get() = if (start <= end) start else end

    operator fun contains(position: CursorPosition): Boolean {
        return start.line <= position.line && position.line <= end.line &&
                start.column <= position.column && position.column <= end.column
    }

    private fun compareCursorPositions(): Int {
        return if (start.line != end.line) {
            start.line - end.line
        } else {
            start.column - end.column
        }
    }

    fun normalize(): CursorRange {
        return if (compareCursorPositions() <= 0) {
            this
        } else {
            CursorRange(this.end, this.start)
        }
    }
}
