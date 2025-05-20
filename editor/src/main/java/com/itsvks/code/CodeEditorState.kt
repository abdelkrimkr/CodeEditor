package com.itsvks.code

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    val rope = Rope.fromFile(File(filePath))
}
