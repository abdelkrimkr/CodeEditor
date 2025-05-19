package com.itsvks.code

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.itsvks.code.core.Direction
import com.itsvks.code.core.EditorSnapshot
import com.itsvks.code.core.TextBuffer
import com.itsvks.code.core.TextPosition
import com.itsvks.code.core.TextPositionRange
import com.itsvks.code.core.deleteLine
import com.itsvks.code.core.getLineSafe
import com.itsvks.code.core.insertLine
import com.itsvks.code.core.swapLines
import com.itsvks.code.language.Language
import com.itsvks.code.language.LanguageRegistry
import com.itsvks.code.language.PlainTextLanguage
import com.itsvks.code.theme.EditorTheme
import com.itsvks.code.theme.VsCodeDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.util.Stack

@Composable
fun rememberCodeEditorState(
    filePath: String,
    language: Language = LanguageRegistry.getLanguageByFileName(filePath) ?: PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    return remember { CodeEditorState(filePath, language, theme) }
}

class CodeEditorState(
    filePath: String,
    language: Language,
    theme: EditorTheme
) {
    private val editorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var buffer = TextBuffer.fromFile(filePath)
    var cursorPosition by mutableStateOf(TextPosition(0, 0))
    private var selectionAnchor: TextPosition? = null
    var selectionRange by mutableStateOf<TextPositionRange?>(null)
        private set

    var language by mutableStateOf(language)
    var theme by mutableStateOf(theme)

    var isCompletionPopupVisible by mutableStateOf(false)
    var completionItems by mutableStateOf(emptyList<String>())

    private var preferredColumn: Int? = null

    private val undoStack = Stack<EditorSnapshot>()
    private val redoStack = Stack<EditorSnapshot>()

    private fun saveSnapshot() {
        undoStack.push(EditorSnapshot(TextBuffer.fromText(buffer.text), cursorPosition, selectionRange))
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(EditorSnapshot(TextBuffer.fromText(buffer.text), cursorPosition, selectionRange))
            val snapshot = undoStack.pop()
            buffer = snapshot.buffer
            cursorPosition = snapshot.cursorPosition
            selectionRange = snapshot.selectionRange
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(EditorSnapshot(TextBuffer.fromText(buffer.text), cursorPosition, selectionRange))
            val snapshot = redoStack.pop()
            buffer = snapshot.buffer
            cursorPosition = snapshot.cursorPosition
            selectionRange = snapshot.selectionRange
        }
    }

    fun moveCursorTo(position: TextPosition) {
        cursorPosition = buffer.clampPosition(position)
        clearSelection()
    }

    fun moveCursorAtEnd() {
        moveCursorTo(buffer.endPosition())
    }

    fun moveCursor(direction: Direction, isShiftPressed: Boolean = false): Boolean {
        val oldPosition = cursorPosition
        val newPosition = calculateNewCursorPosition(direction)

        if (isShiftPressed) {
            if (selectionAnchor == null) selectionAnchor = oldPosition
            selectionRange = TextPositionRange(selectionAnchor!!, newPosition).normalize()
        } else {
            clearSelection()
            preferredColumn = null
        }

        val positionChanged = newPosition != oldPosition
        cursorPosition = newPosition
        return positionChanged
    }

    private fun calculateNewCursorPosition(direction: Direction): TextPosition {
        return when (direction) {
            Direction.LEFT -> {
                preferredColumn = null
                if (cursorPosition.column > 0) {
                    cursorPosition.copy(column = cursorPosition.column - 1)
                } else if (cursorPosition.line > 0) {
                    val prevLine = buffer.getLineSafe(cursorPosition.line - 1)
                    TextPosition(cursorPosition.line - 1, prevLine!!.length)
                } else cursorPosition
            }

            Direction.RIGHT -> {
                preferredColumn = null
                val currentLine = buffer.getLineSafe(cursorPosition.line) ?: ""
                if (cursorPosition.column < currentLine.length) {
                    cursorPosition.copy(column = cursorPosition.column + 1)
                } else {
                    val nextLine = buffer.getLineSafe(cursorPosition.line + 1, null)
                    if (nextLine != null) TextPosition(cursorPosition.line + 1, 0) else cursorPosition
                }
            }

            Direction.UP -> {
                val targetLine = cursorPosition.line - 1
                if (targetLine >= 0) {
                    val lineText = buffer.getLineSafe(targetLine)
                    val col = preferredColumn ?: cursorPosition.column
                    preferredColumn = col
                    TextPosition(targetLine, minOf(col, lineText!!.length))
                } else cursorPosition
            }

            Direction.DOWN -> {
                val targetLine = cursorPosition.line + 1
                val lineText = buffer.getLineSafe(targetLine, null)
                if (lineText != null) {
                    val col = preferredColumn ?: cursorPosition.column
                    preferredColumn = col
                    TextPosition(targetLine, minOf(col, lineText.length))
                } else cursorPosition
            }
        }
    }

    fun clearSelection() {
        selectionRange = null
        selectionAnchor = null
    }

    fun selectRange(range: TextPositionRange) {
        selectionRange = range.normalize()
    }

    fun isTextSelected(): Boolean = selectionRange != null

    fun insertText(text: String) {
        saveSnapshot()
        if (selectionRange != null) {
            buffer.deleteRange(selectionRange!!)
            cursorPosition = selectionRange!!.start
            clearSelection()
        }
        buffer.insertText(cursorPosition, text)
        cursorPosition = buffer.advancePosition(cursorPosition, text)
    }

    fun deleteBackward() {
        saveSnapshot()
        if (selectionRange != null) {
            buffer.deleteRange(selectionRange!!)
            moveCursorTo(selectionRange!!.start)
        } else {
            cursorPosition = buffer.deleteBackward(cursorPosition)
        }
    }

    fun deleteForward() {
        saveSnapshot()
        if (selectionRange != null) {
            buffer.deleteRange(selectionRange!!)
            moveCursorTo(selectionRange!!.start)
        } else {
            cursorPosition = buffer.deleteForward(cursorPosition)
        }
    }

    fun deleteSelectedText() {
        saveSnapshot()
        selectionRange?.let {
            buffer.deleteRange(it)
            moveCursorTo(it.start)
            clearSelection()
        }
    }

    fun deleteSelectionOrBackspace() {
        saveSnapshot()
        if (isTextSelected()) {
            deleteSelectedText()
        } else {
            cursorPosition = buffer.deleteBackward(cursorPosition)
        }
    }

    fun getSelectedText(): String = selectionRange?.let { buffer.slice(it) } ?: ""

    suspend fun setText(text: String) {
        saveSnapshot()
        val normalizedText = withContext(Dispatchers.IO) {
            text.replace("\t", "    ").replace("\r\n", "\n")
        }
        buffer.clear()
        buffer.close()
        buffer = TextBuffer.fromText(normalizedText)
        moveCursorTo(TextPosition(0, 0))
    }

    fun updateCompletions(items: List<String>) {
        completionItems = items
        isCompletionPopupVisible = items.isNotEmpty()
    }

    fun dismissCompletions() {
        isCompletionPopupVisible = false
        completionItems = emptyList()
    }

    fun selectWordAtCursor() {
        val line = buffer.getLineSafe(cursorPosition.line) ?: return

        val (startCol, endCol) = findWordBounds(line, cursorPosition.column)
        selectRange(
            TextPositionRange(
                TextPosition(cursorPosition.line, startCol),
                TextPosition(cursorPosition.line, endCol)
            )
        )
    }

    fun duplicateLine() {
        saveSnapshot()
        val line = buffer.getLineSafe(cursorPosition.line) ?: return
        buffer.insertLine(cursorPosition.line + 1, line)
    }

    fun deleteCurrentLine() {
        saveSnapshot()
        buffer.deleteLine(cursorPosition.line)
        moveCursorTo(TextPosition(cursorPosition.line.coerceAtMost(buffer.lineCount - 1), 0))
    }

    fun moveLineUp() {
        if (cursorPosition.line > 0) {
            saveSnapshot()
            buffer.swapLines(cursorPosition.line, cursorPosition.line - 1)
            moveCursorTo(cursorPosition.copy(line = cursorPosition.line - 1))
        }
    }

    fun moveLineDown() {
        if (cursorPosition.line < buffer.lineCount - 1) {
            saveSnapshot()
            buffer.swapLines(cursorPosition.line, cursorPosition.line + 1)
            moveCursorTo(cursorPosition.copy(line = cursorPosition.line + 1))
        }
    }

    private fun findWordBounds(line: String, col: Int): Pair<Int, Int> {
        if (line.isBlank()) return 0 to 0
        val isWordChar: (Char) -> Boolean = { it.isLetterOrDigit() || it == '_' }

        var start = col
        while (start > 0 && isWordChar(line[start - 1])) start--

        var end = col
        while (end < line.length && isWordChar(line[end])) end++

        return start to end
    }
}
