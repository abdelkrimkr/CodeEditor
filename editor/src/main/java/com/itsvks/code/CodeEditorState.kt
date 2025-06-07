package com.itsvks.code

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.itsvks.code.core.FoldableRange
import com.itsvks.code.core.findBraceFoldableRanges
// import com.itsvks.code.core.emptyStringBuilder // Not used anymore
// import com.itsvks.code.core.getLine // Still used via CodeEditorState.getLine()
// import com.itsvks.code.core.insertLineAt // Still used via CodeEditorState.insertLine()
// import com.itsvks.code.core.lineCount // Still used via CodeEditorState.lineCount
// import com.itsvks.code.core.removeLineAt // Still used via CodeEditorState.removeLine()
// import com.itsvks.code.core.updateLine // Still used via CodeEditorState.updateLine()
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
            initialLines = emptyList(),
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
            initialLines = emptyList(),
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
        CodeEditorState(text.lines(), language, theme)
    }
}

class CodeEditorState(
    initialLines: List<String> = emptyList(),
    initialLanguage: Language = PlainTextLanguage,
    initialTheme: EditorTheme = AtomOneDarkTheme
) {
    private var _lines: List<String> = initialLines
        set(value) {
            field = value
            _foldableRanges.value = findBraceFoldableRanges(field)
            // Decide on _foldedLines reconciliation strategy for direct _lines updates if any outside setText/setFile etc.
        }

    private val _content = MutableStateFlow<List<String>>(initialLines)
    val content: StateFlow<List<String>> = _content.asStateFlow()

    private val _foldableRanges = MutableStateFlow<List<FoldableRange>>(emptyList())
    val foldableRanges: StateFlow<List<FoldableRange>> = _foldableRanges.asStateFlow()

    private val _foldedLines = MutableStateFlow<Set<Int>>(emptySet())
    val foldedLines: StateFlow<Set<Int>> = _foldedLines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var language by mutableStateOf(initialLanguage)
    var theme by mutableStateOf(initialTheme)

    val lineCount get() = _lines.size

    init {
        // Initial calculation for foldable ranges when state is created with initialLines
        _foldableRanges.value = findBraceFoldableRanges(_lines)
    }

    fun setText(text: String) {
        _isLoading.update { false }
        _lines = text.lines()
        // _foldableRanges.value is updated by _lines setter
        _foldedLines.value = emptySet()
        _content.update { _lines }
    }

    suspend fun setFile(file: File) {
        _isLoading.update { true }
        // Temporary state while loading
        val loadingLines = listOf("Loading...")
        _lines = loadingLines
        // _foldableRanges.value is updated by _lines setter for loadingLines
        _foldedLines.value = emptySet()
        _content.update { _lines } // Show "Loading..."

        try {
            val newLines = file.useLines { it.toList() }
            _lines = newLines
            // _foldableRanges.value is updated by _lines setter for newLines
            _foldedLines.value = emptySet() // Reset folds for new file
            _content.update { _lines }
        } finally {
            _isLoading.update { false }
        }
    }

    suspend fun setInputStream(inputStream: InputStream) {
        _isLoading.update { true }
        // Temporary state while loading
        val loadingLines = listOf("Loading...")
        _lines = loadingLines
        // _foldableRanges.value is updated by _lines setter for loadingLines
        _foldedLines.value = emptySet()
        _content.update { _lines } // Show "Loading..."

        try {
            val newLines = inputStream.bufferedReader(Charsets.UTF_8).useLines { it.toList() }
            _lines = newLines
            // _foldableRanges.value is updated by _lines setter for newLines
            _foldedLines.value = emptySet() // Reset folds for new stream
            _content.update { _lines }
        } finally {
            _isLoading.update { false }
        }
    }

    private fun updateLinesAndFolds(newLines: List<String>) {
        _lines = newLines
        // _foldableRanges.value is updated by _lines setter
        val newFoldedLines = _foldedLines.value.filter { foldedStartLine ->
            _foldableRanges.value.any { it.startLine == foldedStartLine }
        }.toSet()
        _foldedLines.value = newFoldedLines
        _content.update { _lines }
    }

    fun removeLine(lineIdx: Int) {
        if (lineIdx in _lines.indices) {
            val mutableLines = _lines.toMutableList()
            mutableLines.removeAt(lineIdx)
            updateLinesAndFolds(mutableLines.toList())
        }
    }

    fun updateLine(lineIdx: Int, text: String) {
        if (lineIdx in _lines.indices) {
            val mutableLines = _lines.toMutableList()
            mutableLines[lineIdx] = text
            updateLinesAndFolds(mutableLines.toList())
        }
    }

    fun insertLine(lineIdx: Int, text: String) {
        val mutableLines = _lines.toMutableList()
        mutableLines.add(if (lineIdx in 0.._lines.size) lineIdx else _lines.size, text)
        updateLinesAndFolds(mutableLines.toList())
    }

    fun getLine(lineIdx: Int): String = _lines[lineIdx]

    fun toggleFold(startLineToToggle: Int) {
        val currentFolded = _foldedLines.value.toMutableSet()
        val rangeExists = _foldableRanges.value.any { it.startLine == startLineToToggle }

        if (!rangeExists) {
            if (currentFolded.contains(startLineToToggle)) {
                currentFolded.remove(startLineToToggle)
                _foldedLines.value = currentFolded
            }
            return
        }

        if (currentFolded.contains(startLineToToggle)) {
            currentFolded.remove(startLineToToggle)
        } else {
            currentFolded.add(startLineToToggle)
        }
        _foldedLines.value = currentFolded
    }
}
