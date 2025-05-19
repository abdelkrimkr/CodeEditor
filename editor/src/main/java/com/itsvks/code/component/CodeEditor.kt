package com.itsvks.code.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    lineHeight: Dp = 20.dp,
    gutterWidth: Dp = 48.dp,
    horizontalPadding: Dp = 8.dp,
    cursorWidth: Float = 2f,
    cursorBlinkDuration: Long = 500,
    blinkCursor: Boolean = true,
    showInvisibleCharacters: Boolean = false
) {

}
