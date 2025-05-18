package com.itsvks.code

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.itsvks.code.core.Direction
import com.itsvks.code.core.TextBuffer
import com.itsvks.code.core.TextPosition
import com.itsvks.code.core.TextPositionRange
import com.itsvks.code.language.Language
import com.itsvks.code.language.LanguageRegistry
import com.itsvks.code.language.PlainTextLanguage
import com.itsvks.code.theme.EditorTheme
import com.itsvks.code.theme.VsCodeDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

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
    language: Language = LanguageRegistry.getLanguageByFileName(filePath) ?: PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
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
    var completionItems by mutableStateOf<List<String>>(emptyList())

    fun moveCursorTo(position: TextPosition) {
        cursorPosition = buffer.clampPosition(position)
        clearSelection()
    }

    fun moveCursorAtEnd() {
        cursorPosition = buffer.endPosition()
        clearSelection()
    }

    fun moveCursor(direction: Direction, isShiftPressed: Boolean = false): Boolean {
        val oldPosition = cursorPosition

        val newPosition = when (direction) {
            Direction.LEFT -> {
                if (cursorPosition.column > 0) {
                    cursorPosition.copy(column = cursorPosition.column - 1)
                } else if (cursorPosition.line > 0) {
                    val prevLineLength = buffer.lines.getOrNull(cursorPosition.line - 1)?.length ?: 0
                    TextPosition(cursorPosition.line - 1, prevLineLength)
                } else {
                    cursorPosition
                }
            }

            Direction.RIGHT -> {
                val currentLine = buffer.lines.getOrNull(cursorPosition.line) ?: ""
                if (cursorPosition.column < currentLine.length) {
                    cursorPosition.copy(column = cursorPosition.column + 1)
                } else if (cursorPosition.line < buffer.lines.size - 1) {
                    TextPosition(cursorPosition.line + 1, 0)
                } else {
                    cursorPosition
                }
            }

            Direction.UP -> {
                if (cursorPosition.line > 0) {
                    val prevLine = buffer.lines.getOrNull(cursorPosition.line - 1) ?: ""
                    val newColumn = minOf(cursorPosition.column, prevLine.length)
                    TextPosition(cursorPosition.line - 1, newColumn)
                } else {
                    cursorPosition
                }
            }

            Direction.DOWN -> {
                if (cursorPosition.line < buffer.lines.size - 1) {
                    val nextLine = buffer.lines.getOrNull(cursorPosition.line + 1) ?: ""
                    val newColumn = minOf(cursorPosition.column, nextLine.length)
                    TextPosition(cursorPosition.line + 1, newColumn)
                } else {
                    cursorPosition
                }
            }
        }

        if (isShiftPressed) {
            if (selectionAnchor == null) selectionAnchor = oldPosition

            selectionRange = TextPositionRange(selectionAnchor!!, newPosition).normalize()
        } else {
            clearSelection()
        }

        val positionChanged = newPosition != oldPosition
        cursorPosition = newPosition
        return positionChanged
    }

    fun selectRange(range: TextPositionRange) {
        selectionRange = range
    }

    fun clearSelection() {
        selectionRange = null
        selectionAnchor = null
    }

    fun insertText(text: String) {
        if (selectionRange != null) {
            buffer.deleteRange(selectionRange!!)
            clearSelection()
        }
        buffer.insertText(cursorPosition, text)
        cursorPosition = buffer.advancePosition(cursorPosition, text)
    }

    fun deleteSelectionOrBackspace() {
        if (selectionRange != null) {
            buffer.deleteRange(selectionRange!!)
            moveCursorTo(selectionRange!!.start)
        } else {
            cursorPosition = buffer.deleteBackward(cursorPosition)
        }
    }

    fun isTextSelected(): Boolean {
        return selectionRange != null
    }

    suspend fun setText(text: String) {
        val clearText = withContext(Dispatchers.IO) {
            text.replace("\t", "    ").replace("\r\n", "\n")
        }

        buffer.clear()
        buffer.close()
        buffer = TextBuffer.fromText(clearText)
        moveCursorTo(TextPosition(0, 0))
    }

    fun deleteForward() {
        if (selectionRange != null) {
            buffer.deleteRange(selectionRange!!)
            moveCursorTo(selectionRange!!.start)
        } else {
            cursorPosition = buffer.deleteForward(cursorPosition)
        }
    }

    fun getSelectedText(): String {
        return selectionRange?.let { buffer.slice(it) } ?: ""
    }

    fun updateCompletions(items: List<String>) {
        completionItems = items
        isCompletionPopupVisible = items.isNotEmpty()
    }

    fun dismissCompletions() {
        isCompletionPopupVisible = false
        completionItems = emptyList()
    }
}

