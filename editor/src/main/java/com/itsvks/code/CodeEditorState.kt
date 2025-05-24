package com.itsvks.code

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.itsvks.code.core.Rope
import com.itsvks.code.language.Language
import com.itsvks.code.language.LanguageRegistry
import com.itsvks.code.language.PlainTextLanguage
import com.itsvks.code.theme.EditorTheme
import com.itsvks.code.theme.VsCodeDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.io.InputStream

@Composable
fun rememberCodeEditorState(
    file: File,
    language: Language = LanguageRegistry.getLanguageByFileName(file.absolutePath) ?: PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    return remember { CodeEditorState(Rope.fromFile(file), language, theme) }
}

@Composable
fun rememberCodeEditorState(
    text: String,
    language: Language = PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    return remember { CodeEditorState(Rope.fromString(text), language, theme) }
}

@Composable
fun rememberCodeEditorState(
    inputStream: InputStream,
    language: Language = PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    return remember { CodeEditorState(Rope.fromInputStream(inputStream), language, theme) }
}

class CodeEditorState(
    rope: Rope,
    language: Language,
    theme: EditorTheme
) {
    private val editorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var _rope by mutableStateOf(rope)
    val rope: Rope get() = _rope

    var language by mutableStateOf(language)
    var theme by mutableStateOf(theme)

    var focusRequest: Pair<Int, Int>? by mutableStateOf(null)
        private set

    fun requestFocusOnLine(lineIndex: Int, cursorPosition: Int) {
        println("Requesting focus on line $lineIndex at $cursorPosition")
        focusRequest = lineIndex to cursorPosition
    }

    fun consumeFocusRequest() {
        focusRequest = null
    }

    fun setText(text: String) {
        _rope = Rope.fromString(text)
    }

    fun setFile(file: File) {
        _rope = Rope.fromFile(file)
    }

    fun setInputStream(inputStream: InputStream) {
        _rope = Rope.fromInputStream(inputStream)
    }

    fun removeLine(lineIdx: Int) {
        val index = rope.getLineStartCharIndex(lineIdx)
        val lineLength = rope.line(lineIdx).length
        println("Before remove: ${rope.line(lineIdx)}")
        _rope = rope.remove(index .. index + lineLength)
        println("After remove: ${rope.line(lineIdx)}")
    }

    fun updateLine(lineIdx: Int, text: String) {
        removeLine(lineIdx)
        val index = rope.getLineStartCharIndex(lineIdx)
        _rope = rope.insert(index, text)
    }

    fun insertLine(lineIdx: Int, text: String) {
        val index = rope.getLineStartCharIndex(lineIdx)
        _rope = rope.insert(index, text)
    }
}
