package com.itsvks.code.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.Closeable

@Composable
fun rememberTextBuffer(initialText: String): TextBuffer {
    return remember(initialText) { TextBuffer.fromText(initialText) }
}

class TextBuffer(private var nativePtr: Long) : Closeable {
    companion object {
        @JvmStatic
        fun fromFile(path: String): TextBuffer {
            val nativePtr = NativeTextBuffer.createRopeFromFile(path)
            return TextBuffer(nativePtr)
        }

        @JvmStatic
        fun fromText(text: String): TextBuffer {
            val nativePtr = NativeTextBuffer.createRope(text)
            return TextBuffer(nativePtr)
        }
    }

    val length: Int
        get() {
            checkRopeIsInitialized()
            return NativeTextBuffer.ropeLen(nativePtr)
        }

    val byteLength: Int
        get() {
            checkRopeIsInitialized()
            return NativeTextBuffer.ropeByteLen(nativePtr)
        }

    val lineCount: Int
        get() {
            checkRopeIsInitialized()
            return NativeTextBuffer.ropeLineCount(nativePtr)
        }

    var text: String
        get() = NativeTextBuffer.ropeToString(nativePtr)
        set(value) {
            if (length > 0) {
                NativeTextBuffer.ropeRemove(nativePtr, 0, length)
            }
            NativeTextBuffer.ropeInsert(nativePtr, 0, value)
        }

    val lines get() = text.split("\n")

    private fun checkRopeIsInitialized() {
        check(nativePtr != 0L) {
            "TextBuffer is not initialized."
        }
    }

    fun getCharAt(index: Int): Char {
        check(index in 0 until length) { "Index out of bounds: $index" }
        checkRopeIsInitialized()
        return NativeTextBuffer.ropeGetChar(nativePtr, index)
    }

    fun getLine(lineIndex: Int): String {
        check(lineIndex in 0 until lineCount) { "Line index out of bounds: $lineIndex" }
        checkRopeIsInitialized()
        return NativeTextBuffer.ropeGetLine(nativePtr, lineIndex)
    }

    fun getLineLength(lineIndex: Int): Int {
        check(lineIndex in 0 until lineCount) { "Line index out of bounds: $lineIndex" }
        checkRopeIsInitialized()
        return NativeTextBuffer.ropeLineLen(nativePtr, lineIndex) - (if (isLastLine(lineIndex)) 0 else 1)
    }

    fun getLineLengthUpTo(lineIndex: Int, column: Int): Int {
        check(lineIndex in 0 until lineCount) { "Line index out of bounds: $lineIndex" }
        val start = TextPosition(lineIndex, 0)
        val end = TextPosition(lineIndex, column)
        return slice(start .. end).length
    }

    fun insert(position: TextPosition, text: String) {
        checkRopeIsInitialized()
        NativeTextBuffer.ropeInsert(nativePtr, position.toIndex(), text)
    }

    fun insertText(position: TextPosition, text: String) {
        insert(position, text)
    }

    fun delete(position: TextPosition) {
        checkRopeIsInitialized()
        val index = position.toIndex()
        if (index in 1 .. length) {
            NativeTextBuffer.ropeRemove(nativePtr, index - 1, index)
        }
    }

    fun deleteRange(range: TextPositionRange) {
        deleteRange(range.start, range.end)
    }

    fun deleteRange(from: TextPosition, to: TextPosition) {
        checkRopeIsInitialized()
        val startIndex = from.toIndex()
        val endIndex = to.toIndex()
        if (startIndex < endIndex) {
            NativeTextBuffer.ropeRemove(nativePtr, startIndex, endIndex)
        }
    }

    fun slice(start: TextPosition, end: TextPosition): String {
        checkRopeIsInitialized()
        val startIndex = start.toIndex()
        val endIndex = end.toIndex()
        return NativeTextBuffer.ropeSlice(nativePtr, startIndex, endIndex)
    }

    fun slice(range: TextPositionRange): String {
        return slice(range.start, range.end)
    }

    fun lineToChar(line: Int): Int {
        checkRopeIsInitialized()
        return NativeTextBuffer.ropeLineToChar(nativePtr, line)
    }

    fun charToLine(charIdx: Int): Int {
        checkRopeIsInitialized()
        return NativeTextBuffer.ropeCharToLine(nativePtr, charIdx)
    }

    fun copy(range: TextPositionRange): String {
        return slice(range)
    }

    fun cut(range: TextPositionRange): String {
        val text = slice(range)
        deleteRange(range)
        return text
    }

    fun paste(position: TextPosition, text: String) {
        insert(position, text)
    }

    fun append(text: String) {
        checkRopeIsInitialized()
        NativeTextBuffer.ropeInsert(nativePtr, length, text)
    }

    fun clear() {
        checkRopeIsInitialized()
        NativeTextBuffer.ropeRemove(nativePtr, 0, length)
    }

    fun isEmpty(): Boolean = length == 0

    fun subSequence(start: TextPosition, end: TextPosition): CharSequence {
        return slice(start, end)
    }

    fun deleteBackward(position: TextPosition): TextPosition {
        checkRopeIsInitialized()
        val index = position.toIndex()
        return if (index > 0) {
            NativeTextBuffer.ropeRemove(nativePtr, index - 1, index)
            indexToPosition(index - 1)
        } else position
    }

    fun deleteForward(position: TextPosition): TextPosition {
        checkRopeIsInitialized()
        val index = position.toIndex()
        return if (index < length) {
            NativeTextBuffer.ropeRemove(nativePtr, index, index + 1)
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
        val line = NativeTextBuffer.ropeCharToLine(nativePtr, index)
        val lineStart = NativeTextBuffer.ropeLineToChar(nativePtr, line)
        return TextPosition(line, index - lineStart)
    }

    fun isLastLine(lineIdx: Int) = lineIdx == lineCount - 1

    fun endPosition(): TextPosition {
        return TextPosition(lineCount - 1, getLineLength(lineCount - 1))
    }

    private fun TextPosition.toIndex(): Int {
        checkRopeIsInitialized()
        val lineStart = NativeTextBuffer.ropeLineToChar(nativePtr, line)
        return lineStart + column
    }

    override fun close() {
        NativeTextBuffer.deleteRope(nativePtr)
        nativePtr = 0
    }
}
