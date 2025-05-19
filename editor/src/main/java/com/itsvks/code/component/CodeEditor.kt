package com.itsvks.code.component

import android.content.ClipData
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsvks.code.CodeEditorState
import com.itsvks.code.core.Direction
import com.itsvks.code.core.TextPosition
import com.itsvks.code.core.TextPositionRange
import com.itsvks.code.input.codeEditorTextInput
import com.itsvks.code.rendering.drawEditorContent
import com.itsvks.code.syntax.SyntaxHighlighterFactory
import com.itsvks.code.util.isPrintable
import com.itsvks.code.util.rememberImeHeight
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
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
    val focusRequester = remember { FocusRequester() }

    var cursorVisible by remember { mutableStateOf(true) }
    var isCursorMoving by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboard.current
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        state.moveCursorAtEnd()
    }

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { fontSize.toPx() }
            typeface = try {
                Typeface.createFromAsset(context.assets, "JetBrainsMono-Regular.ttf")
            } catch (e: Exception) {
                Typeface.MONOSPACE
            }
            color = state.theme.defaultTextColor.toArgb()
            letterSpacing = 0f
        }
    }

    val gutterPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { fontSize.toPx() }
            typeface = try {
                Typeface.createFromAsset(context.assets, "JetBrainsMono-Regular.ttf")
            } catch (e: Exception) {
                Typeface.MONOSPACE
            }
            color = state.theme.gutterTextColor.toArgb()
            textAlign = Paint.Align.RIGHT
        }
    }

    val cursorAlpha by animateFloatAsState(
        targetValue = if (cursorVisible && !isCursorMoving) 0.8f else 0f, // Blink only when not moving
        label = "cursorAlpha"
    )

    val charWidth = textPaint.measureText("W")
    var scrollableBoxSize by remember { mutableStateOf(IntSize.Zero) }
    val focusInteractionSource = remember { MutableInteractionSource() }

    val syntaxHighlighter = remember(state.language) {
        SyntaxHighlighterFactory.createHighlighter(state.language)
    }

    val imeHeight by rememberImeHeight()

    // Completion popup state
    var showCompletions by remember { mutableStateOf(false) }

    LaunchedEffect(isCursorMoving, blinkCursor) {
        if (isCursorMoving || !blinkCursor) {
            cursorVisible = true
        } else {
            while (true) {
                cursorVisible = !cursorVisible
                delay(cursorBlinkDuration)
            }
        }
    }

    LaunchedEffect(state.cursorPosition) {
        isCursorMoving = true
        delay(500)
        isCursorMoving = false
    }

    Box(modifier = modifier) {
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        LaunchedEffect(state.cursorPosition) {
            val line = state.cursorPosition.line
            val lineHeightPx = with(density) { lineHeight.toPx() }

            val cursorY = (line * lineHeightPx).toInt()
            val visibleTop = verticalScrollState.value
            val visibleBottom = verticalScrollState.value + verticalScrollState.maxValue / verticalScrollState.maxValue.coerceAtLeast(1) * verticalScrollState.maxValue
            val padding = (lineHeightPx * 2).toInt()

            if (cursorY < visibleTop + padding) {
                verticalScrollState.animateScrollTo((cursorY - padding).coerceAtLeast(0))
            } else if (cursorY + lineHeightPx > visibleBottom - padding) {
                verticalScrollState.animateScrollTo((cursorY + padding - (visibleBottom - visibleTop)).coerceAtMost(verticalScrollState.maxValue))
            }

            val cursorX = (state.buffer.getLineLengthUpTo(line, state.cursorPosition.column) * charWidth).toInt()
            val visibleLeft = horizontalScrollState.value
            val visibleRight = visibleLeft + horizontalScrollState.maxValue / horizontalScrollState.maxValue.coerceAtLeast(1) * horizontalScrollState.maxValue

            if (cursorX < visibleLeft + padding) {
                horizontalScrollState.animateScrollTo((cursorX - padding).coerceAtLeast(0))
            } else if (cursorX > visibleRight - padding) {
                horizontalScrollState.animateScrollTo((cursorX + padding - (visibleRight - visibleLeft)).coerceAtMost(horizontalScrollState.maxValue))
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            val maxLineLength = state.buffer.lines.maxOfOrNull { it.length } ?: 0
            val contentWidth = (maxLineLength * charWidth).dp
            val lineHeightPx = with(density) { lineHeight.toPx() }
            val contentHeight = (state.buffer.lineCount * lineHeightPx).dp - imeHeight

            // Gutter
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(verticalScrollState)
                    .background(state.theme.gutterBgColor)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(gutterWidth)
                        .height(contentHeight)
                ) {
                    val lines = state.buffer.lineCount

                    val firstVisibleLine = (verticalScrollState.value / lineHeightPx).toInt().coerceAtLeast(0)
                    val lastVisibleLine = ((verticalScrollState.value + size.height) / lineHeightPx).toInt().coerceAtMost(lines - 1)

                    for (lineIndex in firstVisibleLine .. lastVisibleLine) {
                        val lineNumber = (lineIndex + 1).toString()

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                lineNumber,
                                size.width - horizontalPadding.toPx(),
                                lineIndex * lineHeightPx + lineHeightPx * 0.8f,
                                gutterPaint
                            )
                        }
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outline)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
                    .imeNestedScroll()
                    .focusRequester(focusRequester)
                    .codeEditorTextInput(
                        state = state,
                        keyboardController = keyboardController,
                        onKeyEvent = {
                            handleKeyEvent(
                                event = it,
                                state = state,
                                clipboard = clipboard
                            )
                        }
                    )
                    .focusable(interactionSource = focusInteractionSource)
                    .background(state.theme.backgroundColor)
                    .onSizeChanged { newSize -> scrollableBoxSize = newSize }
            ) {
                Canvas(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(contentHeight)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    focusRequester.requestFocus()

                                    state.cursorPosition = calculateTextPosition(
                                        offset = offset,
                                        state = state,
                                        density = density,
                                        lineHeight = lineHeight,
                                        horizontalPadding = horizontalPadding,
                                        charWidth = charWidth,
                                        scrollY = verticalScrollState.value.toFloat()
                                    )

                                    state.clearSelection()
                                },
                                onDoubleTap = { offset ->
                                    focusRequester.requestFocus()

                                    state.cursorPosition = calculateTextPosition(
                                        offset = offset,
                                        state = state,
                                        density = density,
                                        lineHeight = lineHeight,
                                        horizontalPadding = horizontalPadding,
                                        charWidth = charWidth,
                                        scrollY = verticalScrollState.value.toFloat()
                                    )

                                    val (wordStart, wordEnd) = findWordBoundaries(state)

                                    state.cursorPosition = wordEnd
                                    state.selectRange(wordStart .. wordEnd)
                                },
                                onLongPress = { offset ->
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    focusRequester.requestFocus()

                                    val newPosition = calculateTextPosition(
                                        offset = offset,
                                        state = state,
                                        density = density,
                                        lineHeight = lineHeight,
                                        horizontalPadding = horizontalPadding,
                                        charWidth = charWidth,
                                        scrollY = verticalScrollState.value.toFloat()
                                    )

                                    state.cursorPosition = newPosition
                                    state.selectRange(findWordBoundaries(state))
                                }
                            )
                        }
                ) {
                    val firstVisibleLine = (verticalScrollState.value / lineHeightPx).toInt().coerceAtLeast(0)
                    val lastVisibleLine = ((verticalScrollState.value + size.height) / lineHeightPx).toInt().coerceAtMost(state.buffer.lineCount - 1)

                    drawEditorContent(
                        buffer = state.buffer,
                        syntaxHighlighter = syntaxHighlighter,
                        textPaint = textPaint,
                        lineHeight = lineHeightPx,
                        charWidth = charWidth,
                        horizontalPadding = horizontalPadding.toPx(),
                        cursorPosition = state.cursorPosition,
                        selectionRange = state.selectionRange,
                        cursorAlpha = if (isCursorMoving) 0.8f else cursorAlpha,
                        cursorWidth = cursorWidth,
                        theme = state.theme,
                        drawInvisibles = showInvisibleCharacters,
                        firstVisibleLine = firstVisibleLine,
                        lastVisibleLine = lastVisibleLine
                    )
                }
            }

            // Auto-completion popup
            if (showCompletions) {
                // ...
            }
        }
    }

    // Request focus to show the keyboard initially
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private suspend fun handleKeyEvent(
    event: KeyEvent,
    state: CodeEditorState,
    clipboard: Clipboard,
    onCursorMoved: (Boolean) -> Unit = {}
): Boolean {
    return when (event.type) {
        KeyEventType.KeyDown -> {
            onCursorMoved(true)

            if (event.isCtrlPressed) {
                handleCtrlKeyEvent(event, state, clipboard)
            } else handleKeyDown(event, state)
        }

        KeyEventType.KeyUp -> {
            onCursorMoved(false)
            true
        }

        else -> false
    }
}

private suspend fun handleCtrlKeyEvent(event: KeyEvent, state: CodeEditorState, clipboard: Clipboard): Boolean {
    return when (event.key) {
        Key.V -> {
            if (state.isTextSelected()) {
                state.deleteSelectionOrBackspace()
            }

            val clipboardText = clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text ?: ""
            state.insertText(clipboardText.toString())
            true
        }

        Key.C -> {
            val text = state.getSelectedText()
            clipboard.setClipEntry(ClipData.newPlainText("Editor", text).toClipEntry())
            true
        }

        Key.X -> {
            val text = state.selectionRange?.let { state.buffer.cut(it) }
            state.clearSelection()
            clipboard.setClipEntry(ClipData.newPlainText("Editor", text).toClipEntry())
            true
        }

        Key.A -> {
            state.selectRange(TextPosition.Zero .. state.buffer.endPosition())
            true
        }

        else -> false
    }
}

private fun handleKeyDown(event: KeyEvent, state: CodeEditorState): Boolean {
    return when (event.key) {
        Key.DirectionLeft -> state.moveCursor(Direction.LEFT, event.isShiftPressed)
        Key.DirectionRight -> state.moveCursor(Direction.RIGHT, event.isShiftPressed)
        Key.DirectionUp -> state.moveCursor(Direction.UP, event.isShiftPressed)
        Key.DirectionDown -> state.moveCursor(Direction.DOWN, event.isShiftPressed)
        Key.Backspace -> {
            state.deleteSelectionOrBackspace()
            true
        }

        Key.Enter -> {
            state.insertText("\n")
            true
        }

        Key.Delete -> {
            state.deleteForward()
            true
        }

        Key.Tab -> {
            state.insertText("    ")
            true
        }

        Key.MoveHome -> {
            state.moveCursorTo(TextPosition(state.cursorPosition.line, 0))
            true
        }

        Key.MoveEnd -> {
            state.moveCursorTo(TextPosition(state.cursorPosition.line, state.buffer.getLineLength(state.cursorPosition.line)))
            true
        }

        else -> if (event.utf16CodePoint > 0) {
            val char = event.utf16CodePoint.toChar()

            if (char.isPrintable()) {
                state.insertText(char.toString())
                true
            } else false
        } else false
    }
}

private fun findWordBoundaries(
    state: CodeEditorState
): TextPositionRange {
    val position = state.cursorPosition

    val line = state.buffer.lines.getOrNull(position.line) ?: return position .. position

    var startCol = position.column.coerceIn(0, line.length)
    while (startCol > 0 && line[startCol - 1].isLetterOrDigit()) {
        startCol--
    }

    var endCol = position.column.coerceIn(0, line.length)
    while (endCol < line.length && line[endCol].isLetterOrDigit()) {
        endCol++
    }

    return TextPosition(position.line, startCol) .. TextPosition(position.line, endCol)
}

private fun calculateTextPosition(
    offset: Offset,
    state: CodeEditorState,
    density: Density,
    lineHeight: Dp,
    horizontalPadding: Dp,
    charWidth: Float,
    scrollY: Float
): TextPosition {
    val line = with(density) {
        ((offset.y + 0) / lineHeight.toPx()).toInt().coerceIn(0, state.buffer.lines.size - 1)
    }

    val lineLength = state.buffer.getLineLength(line)

    val xOffset = with(density) {
        (offset.x - horizontalPadding.toPx()).coerceAtLeast(0f)
    }

    val column = (xOffset / charWidth).toInt().coerceIn(0, lineLength)
    return TextPosition(line, column)
}

private fun Density.calculateAutoCompletePosition(
    state: CodeEditorState,
    lineHeight: Dp,
    horizontalPadding: Dp,
    gutterWidth: Dp,
    charWidth: Float
): IntOffset {
    val cursorPosition = state.cursorPosition

    return IntOffset(
        x = ((cursorPosition.column * charWidth) + gutterWidth.toPx() + horizontalPadding.toPx()).toInt(),
        y = ((cursorPosition.line + 1) * lineHeight.toPx()).toInt()
    )
}
