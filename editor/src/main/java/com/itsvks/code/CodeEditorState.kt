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
    stream: InputStream,
    language: Language = PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    return remember { CodeEditorState(Rope.fromStream(stream), language, theme) }
}

class CodeEditorState(
    rope: Rope,
    language: Language,
    theme: EditorTheme
) {
    private val editorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var rope by mutableStateOf(rope)
    var language by mutableStateOf(language)
    var theme by mutableStateOf(theme)

    fun setText(text: String) {
        rope = Rope.fromString(text)
    }

    fun setFile(file: File) {
        rope = Rope.fromFile(file)
    }

    fun setInputStream(inputStream: InputStream) {
        rope = Rope.fromStream(inputStream)
    }
}
