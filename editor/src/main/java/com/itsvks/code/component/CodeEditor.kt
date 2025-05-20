package com.itsvks.code.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsvks.code.CodeEditorState

@Composable
fun CodeEditor(
    state: CodeEditorState,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    gutterWidth: Dp = 48.dp,
    horizontalPadding: Dp = 8.dp,
    cursorWidth: Float = 2f,
    cursorBlinkDuration: Long = 500,
    blinkCursor: Boolean = true,
    showInvisibleCharacters: Boolean = false
) {
    val verticalScrollState = rememberScrollState()

    Box(modifier = modifier.verticalScroll(verticalScrollState)) {
        EditorCanvas(
            state = state,
            fontSize = fontSize,
            scrollY = verticalScrollState.value,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun EditorCanvas(
    state: CodeEditorState,
    fontSize: TextUnit,
    scrollY: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val density = LocalDensity.current
    val lineHeightPx = with(density) { fontSize.toPx() * 1.5f }

    Canvas(modifier = modifier) {
        val visibleLines = (scrollY / lineHeightPx).toInt()..((scrollY + size.height) / lineHeightPx).toInt()
    }
}
