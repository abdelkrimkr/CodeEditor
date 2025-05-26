package com.itsvks.code

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.itsvks.code.core.emptyStringBuilder
import com.itsvks.code.core.getLine
import com.itsvks.code.core.insertLineAt
import com.itsvks.code.core.lineCount
import com.itsvks.code.core.removeLineAt
import com.itsvks.code.core.toStringBuilder
import com.itsvks.code.core.updateLine
import com.itsvks.code.language.Language
import com.itsvks.code.language.LanguageRegistry
import com.itsvks.code.language.PlainTextLanguage
import com.itsvks.code.theme.AtomOneDarkTheme
import com.itsvks.code.theme.EditorTheme
import com.itsvks.code.theme.VsCodeDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream

@Composable
fun rememberCodeEditorState(
    file: File,
    initialLanguage: Language = LanguageRegistry.getLanguageByFileName(file.absolutePath) ?: PlainTextLanguage,
    initialTheme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    val state = remember {
        CodeEditorState(
            initialContentSb = emptyStringBuilder(),
            initialLanguage = initialLanguage,
            initialTheme = initialTheme
        )
    }

    LaunchedEffect(file, initialLanguage, initialTheme) {
        state.language = initialLanguage
        state.theme = initialTheme
        try {
            state.setFile(file)
        } catch (e: IOException) {
            Log.e("CodeEditorState", "Error loading file ${file.absolutePath}", e)
            state.setText("Error loading file: ${e.message}")
        }
    }
    return state
}

@Composable
fun rememberCodeEditorState(
    inputStream: InputStream,
    initialLanguage: Language = PlainTextLanguage,
    initialTheme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    val state = remember {
        CodeEditorState(
            initialContentSb = emptyStringBuilder(),
            initialLanguage = initialLanguage,
            initialTheme = initialTheme
        )
    }

    LaunchedEffect(inputStream, initialLanguage, initialTheme) {
        state.language = initialLanguage
        state.theme = initialTheme
        try {
            state.setInputStream(inputStream)
        } catch (e: IOException) {
            Log.e("CodeEditorState", "Error loading input stream", e)
            state.setText("Error loading input stream: ${e.message}")
        }
    }
    return state
}

@Composable
fun rememberCodeEditorState(
    text: String,
    language: Language = PlainTextLanguage,
    theme: EditorTheme = VsCodeDarkTheme
): CodeEditorState {
    return remember(text, language, theme) {
        CodeEditorState(text.toStringBuilder(), language, theme)
    }
}

class CodeEditorState(
    initialContentSb: StringBuilder = emptyStringBuilder(),
    initialLanguage: Language = PlainTextLanguage,
    initialTheme: EditorTheme = AtomOneDarkTheme
) {
    private var _internalStringBuilder: StringBuilder = initialContentSb

    private val _content = MutableStateFlow(_internalStringBuilder)
    val content = _content.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var language by mutableStateOf(initialLanguage)
    var theme by mutableStateOf(initialTheme)

    val lineCount get() = _internalStringBuilder.lineCount

    private fun updateContent(newSb: StringBuilder) {
        _internalStringBuilder = newSb
        _content.update { _internalStringBuilder }
    }

    fun setText(text: String) {
        _isLoading.update { false }
        updateContent(text.toStringBuilder())
    }

    suspend fun setFile(file: File) {
        _isLoading.update { true }
        updateContent("Loading...".toStringBuilder())
        try {
            val newContentSb = file.toStringBuilder()
            updateContent(newContentSb)
        } finally {
            _isLoading.update { false }
        }
    }

    suspend fun setInputStream(inputStream: InputStream) {
        _isLoading.update { true }
        updateContent("Loading...".toStringBuilder())
        try {
            val newContentSb = inputStream.toStringBuilder()
            updateContent(newContentSb)
        } finally {
            _isLoading.update { false }
        }
    }

    fun removeLine(lineIdx: Int) {
        updateContent(_internalStringBuilder.removeLineAt(lineIdx))
    }

    fun updateLine(lineIdx: Int, text: String) {
        updateContent(_internalStringBuilder.updateLine(lineIdx, text))
    }

    fun insertLine(lineIdx: Int, text: String) {
        updateContent(_internalStringBuilder.insertLineAt(lineIdx, text))
    }

    fun getLine(lineIdx: Int): String {
        return _internalStringBuilder.getLine(lineIdx)
    }
}
