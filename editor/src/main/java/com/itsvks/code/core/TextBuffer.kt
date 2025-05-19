package com.itsvks.code.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File

@Composable
fun rememberTextBuffer(initialText: String): TextBuffer {
    return remember(initialText) { TextBuffer.fromText(initialText) }
}

class TextBuffer private constructor(private var rope: Rope) {
    companion object {
        @JvmStatic
        fun fromFile(path: String): TextBuffer {
            val rope = Rope.fromFile(File(path))
            return TextBuffer(rope)
        }

        @JvmStatic
        fun fromText(text: String): TextBuffer {
            val rope = Rope.fromString(text)
            return TextBuffer(rope)
        }
    }

    val length: Int get() = rope.length
    val lineCount: Int get() = rope.lineCount

    val maxLineLength: Int get() = rope.maxLineLength()

    fun clone(): TextBuffer = TextBuffer(rope.clone())

    fun getCharAt(index: Int): Char = rope.charAt(index)

    fun getCharAt(position: TextPosition): Char = getCharAt(position.toIndex())

    fun getLine(lineIndex: Int): String = rope.line(lineIndex).toPlainString()

    fun getLineLength(lineIndex: Int): Int = rope.lineLength(lineIndex)

//    fun getLineLengthUpTo(lineIndex: Int, column: Int): Int = rope.lineLengthUpTo(lineIndex, column)

    fun insert(position: TextPosition, text: String) {
        rope = rope.insert(position.toIndex(), text)
    }

    fun insertText(position: TextPosition, text: String) = insert(position, text)

    fun delete(position: TextPosition) {
        val index = position.toIndex()
        if (index in 1 .. length) {
            rope = rope.remove(index - 1, index)
        }
    }

    fun deleteRange(range: TextPositionRange) = deleteRange(range.start, range.end)

    fun deleteRange(from: TextPosition, to: TextPosition) {
        val startIndex = from.toIndex()
        val endIndex = to.toIndex()
        if (startIndex < endIndex) {
            rope = rope.remove(startIndex, endIndex)
        }
    }

    fun slice(start: TextPosition, end: TextPosition): String {
        val startIndex = start.toIndex()
        val endIndex = end.toIndex()
        return rope.slice(startIndex, endIndex).toPlainString()
    }

    fun slice(range: TextPositionRange): String = slice(range.start, range.end)

    fun lineToChar(line: Int): Int = rope.lineToChar(line)

    fun charToLine(charIdx: Int): Int = rope.charToLine(charIdx)

    fun copy(range: TextPositionRange): String = slice(range)

    fun cut(range: TextPositionRange): String {
        val text = slice(range)
        deleteRange(range)
        return text
    }

    fun paste(position: TextPosition, text: String) = insert(position, text)

    fun append(text: String) {
        rope = rope.insert(length, text)
    }

    fun clear() {
        rope = rope.remove(0, length)
    }

    fun isEmpty(): Boolean = length == 0

    fun subSequence(start: TextPosition, end: TextPosition): CharSequence = slice(start, end)

    fun charBefore(position: TextPosition): Char? {
        val index = position.toIndex()
        return if (index > 0) getCharAt(index - 1) else null
    }

    fun deleteBackward(position: TextPosition): TextPosition {
        val index = position.toIndex()
        if (index <= 0) return position

        val char = rope.charAt(index - 1)
        return if (char == '\r' && index >= 2) {
            rope = rope.remove(index - 2, index)
            indexToPosition(index - 2)
        } else {
            rope = rope.remove(index - 1, index)
            indexToPosition(index - 1)
        }
    }

    fun deleteForward(position: TextPosition): TextPosition {
        val index = position.toIndex()
        return if (index < length) {
            rope = rope.remove(index, index + 1)
            position
        } else position
    }

    fun replace(range: TextPositionRange, replacement: String) {
        deleteRange(range)
        insert(range.start, replacement)
    }

    fun clampPosition(pos: TextPosition): TextPosition {
        val line = pos.line.coerceIn(0, lineCount - 1)
        val column = pos.column.coerceIn(0, getLineLength(line))
        return TextPosition(line, column)
    }

    fun advancePosition(pos: TextPosition, text: String): TextPosition {
        val lines = text.split("\n")
        return if (lines.size == 1) {
            TextPosition(pos.line, pos.column + lines[0].length)
        } else {
            TextPosition(pos.line + lines.size - 1, lines.last().length)
        }
    }

    fun indexToPosition(index: Int): TextPosition {
        val line = rope.charToLine(index)
        val lineStart = rope.lineToChar(line)
        return TextPosition(line, index - lineStart)
    }

    fun isLastLine(lineIdx: Int) = lineIdx == lineCount - 1

    fun endPosition(): TextPosition = TextPosition(lineCount - 1, getLineLength(lineCount - 1))

    fun getFullText() = rope.toPlainString()

    private fun TextPosition.toIndex(): Int {
        val lineStart = rope.lineToChar(line)
        return lineStart + column
    }
}
