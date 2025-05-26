package com.itsvks.code.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsvks.code.CodeEditorState
import com.itsvks.code.core.bracketPairs
import com.itsvks.code.core.getLine
import com.itsvks.code.core.lineCount
import com.itsvks.code.core.rememberJetBrainsMonoFontFamily
import com.itsvks.code.syntax.SyntaxHighlighterFactory
import com.itsvks.code.theme.EditorTheme
import com.itsvks.code.theme.findBracketPairIndices
import com.itsvks.code.theme.highlight
import kotlinx.coroutines.Dispatchers
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import kotlin.math.max

@Composable
fun CodeEditor(
    state: CodeEditorState,
    modifier: Modifier = Modifier,
    initialFontSize: TextUnit = 14.sp,
    softWrap: Boolean = false,
    gutterWidth: Dp = 48.dp,
    horizontalPadding: Dp = 6.dp,
    editable: Boolean = false,
    enableZoomGestures: Boolean = false
) {
    var fontSize by remember { mutableStateOf(initialFontSize) }
    val fontFamily = rememberJetBrainsMonoFontFamily()
    val listState = rememberLazyListState()
    val theme = state.theme
    val content by state.content.collectAsState(Dispatchers.IO)
    val isLoading by state.isLoading.collectAsState(Dispatchers.IO)

    val syntaxHighlighter by remember {
        derivedStateOf {
            SyntaxHighlighterFactory.createHighlighter(state.language)
        }
    }

    val selectionColors by remember {
        derivedStateOf {
            TextSelectionColors(
                handleColor = theme.selectionHandleColor,
                backgroundColor = theme.selectionColor
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (enableZoomGestures) Modifier.pointerInput(Unit) {
                    detectTransformGestures(panZoomLock = true) { _, _, zoom, _ ->
                        fontSize *= zoom
                    }
                } else Modifier
            )
    ) {
        SelectionContainer {
            CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                LazyColumnScrollbar(
                    state = listState,
                    settings = ScrollbarSettings.Default.copy(
                        hideDelayMillis = 1000,
                        scrollbarPadding = 0.dp,
                        thumbThickness = 10.dp,
                        thumbSelectedColor = theme.scrollBarSelectedColor,
                        thumbUnselectedColor = theme.scrollBarColor,
                        thumbShape = RectangleShape
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (!softWrap) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                            .background(theme.backgroundColor),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(content.lineCount, key = { it.hashCode() }) { lineIdx ->
                            Row {
                                var isLineFocused by remember { mutableStateOf(false) }
                                var lineHeight by remember { mutableIntStateOf(0) }
                                val lineStr = (lineIdx + 1).toString()

                                Box(
                                    contentAlignment = Alignment.TopEnd,
                                    modifier = Modifier
                                        .width(gutterWidth)
                                        .padding(end = horizontalPadding)
                                        .drawWithCache {
                                            onDrawBehind {
                                                val width = size.width + horizontalPadding.toPx()
                                                val height = max(size.height, lineHeight.toFloat())

                                                drawRect(
                                                    color = if (isLineFocused) theme.activeLineColor else theme.gutterBgColor,
                                                    topLeft = Offset(0f, (-0.5f).dp.toPx()),
                                                    size = Size(width, height + 1.5f.dp.toPx())
                                                )

                                                drawLine(
                                                    color = theme.gutterBorderColor,
                                                    start = Offset(width, (-0.5f).dp.toPx()),
                                                    end = Offset(width, height + 1.5f.dp.toPx()),
                                                    strokeWidth = 1f
                                                )
                                            }
                                        }
                                ) {
                                    DisableSelection {
                                        Text(
                                            text = lineStr,
                                            color = theme.gutterTextColor,
                                            fontSize = fontSize,
                                            fontFamily = fontFamily,
                                            lineHeight = fontSize,
                                            softWrap = false,
                                            overflow = TextOverflow.Visible
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = horizontalPadding)
                                ) {
                                    var textFieldValue by remember(lineIdx, content) {
                                        mutableStateOf(TextFieldValue(content.getLine(lineIdx).highlight(theme, syntaxHighlighter)))
                                    }

                                    BasicTextField(
                                        value = textFieldValue,
                                        onValueChange = {
                                            var updated = handleAutoIndent(it, textFieldValue)
                                            updated = handleBracketPairMatch(updated, textFieldValue)

                                            textFieldValue = TextFieldValue(
                                                updated.text.highlight(
                                                    theme = theme,
                                                    syntaxHighlighter = syntaxHighlighter,
                                                    bracketIndices = findBracketIndices(updated)
                                                ),
                                                selection = updated.selection,
                                                composition = updated.composition
                                            )
                                        },
                                        singleLine = !softWrap,
                                        readOnly = !editable,
                                        textStyle = TextStyle(
                                            fontSize = fontSize,
                                            fontFamily = fontFamily,
                                            color = theme.defaultTextColor
                                        ),
                                        onTextLayout = {
                                            lineHeight = it.size.height
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                isLineFocused = it.isFocused

                                                textFieldValue = textFieldValue.copy(
                                                    textFieldValue.annotatedString.highlight(
                                                        theme = theme,
                                                        syntaxHighlighter = syntaxHighlighter,
                                                        bracketIndices = if (it.isFocused) findBracketIndices(textFieldValue) else emptySet()
                                                    )
                                                )
                                            },
                                        cursorBrush = SolidColor(theme.cursorColor),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .drawActiveLineColor(isLineFocused, theme, horizontalPadding)
                                            ) {
                                                innerTextField()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "rotation")

            val angleOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                ),
                label = "angleOffset"
            )

            Spacer(
                modifier = Modifier
                    .size(20.dp)
                    .padding(4.dp)
                    .drawWithCache {
                        onDrawBehind {
                            val sweepAngle = 270f
                            val diameter = size.minDimension
                            val stroke = Stroke(width = 4f, cap = StrokeCap.Round)

                            drawArc(
                                color = theme.cursorColor,
                                startAngle = angleOffset,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(
                                    (size.width - diameter) / 2f,
                                    (size.height - diameter) / 2f
                                ),
                                size = Size(diameter, diameter),
                                style = stroke
                            )
                        }
                    }
            )
        }
    }
}

private fun Modifier.drawActiveLineColor(
    isLineFocused: Boolean,
    theme: EditorTheme,
    horizontalPadding: Dp
) = this then if (isLineFocused) Modifier.drawWithCache {
    onDrawBehind {
        drawRect(
            color = theme.activeLineColor,
            topLeft = Offset(-horizontalPadding.toPx() + 1, 0f)//+1 for gutter border width
        )
    }
} else Modifier

private fun handleBracketPairMatch(
    newValue: TextFieldValue,
    oldValue: TextFieldValue
): TextFieldValue {
    val text = newValue.text
    val cursor = newValue.selection.start
    val oldCursor = oldValue.selection.start
    val oldText = oldValue.text

    val typedChar = if (cursor > oldCursor && oldCursor in oldText.indices) {
        text.getOrNull(oldCursor)
    } else null

    val isClosingBracket = typedChar in bracketPairs.values
    val atCursorChar = text.getOrNull(cursor)

    val skipClosing = isClosingBracket && typedChar == atCursorChar &&
            atCursorChar !in listOf('"', '\'')

    val updatedTextFieldValue = when {
        // skip over closing bracket if already present
        skipClosing -> {
            TextFieldValue(
                text = oldText,
                selection = TextRange(cursor + 1),
                composition = newValue.composition
            )
        }

        // insert closing pair only if a new bracket was just typed
        cursor > 0 &&
                text.length == oldText.length + 1 &&
                cursor > oldCursor &&
                bracketPairs.containsKey(text.getOrNull(cursor - 1)) &&
                text.getOrNull(cursor) != bracketPairs[text[cursor - 1]] -> {

            val open = text[cursor - 1]
            val close = bracketPairs[open]!!

            val before = text.substring(0, cursor)
            val after = text.substring(cursor)

            TextFieldValue(
                text = before + close + after,
                selection = TextRange(cursor, cursor),
                composition = newValue.composition
            )
        }

        else -> newValue
    }
    return updatedTextFieldValue
}

private fun handleAutoIndent(
    newValue: TextFieldValue,
    oldValue: TextFieldValue
): TextFieldValue {
    val oldText = oldValue.text
    val newText = newValue.text
    val cursor = newValue.selection.start
    val oldCursor = oldValue.selection.start

    val isNewline = cursor > 0 &&
            cursor > oldCursor &&
            newText.length > oldText.length &&
            newText.getOrNull(cursor - 1) == '\n'

    if (!isNewline) return newValue

    val before = newText.substring(0, cursor)
    val after = newText.substring(cursor)

    val lines = before.lines()
    val prevLine = lines.getOrNull(lines.size - 2) ?: ""
    val indentMatch = Regex("^[ \t]*").find(prevLine)
    val baseIndent = indentMatch?.value ?: ""

    val trimmedPrev = prevLine.trimEnd()
    val extraIndent = if (trimmedPrev.endsWith("{") || trimmedPrev.endsWith("[") || trimmedPrev.endsWith("(")) {
        "    "
    } else ""

    val currentIndent = baseIndent + extraIndent
    val nextChar = after.firstOrNull()

    val isClosingBracket = nextChar in listOf(')', '}', ']')

    if (isClosingBracket && currentIndent.length >= 4) {
        val closingLine = baseIndent + nextChar + after.drop(1)
        val finalText = before + currentIndent + "\n" + closingLine
        val finalCursor = (before + currentIndent).length

        return TextFieldValue(
            text = finalText,
            selection = TextRange(finalCursor),
            composition = newValue.composition
        )
    }

    val finalText = before + currentIndent + after
    val finalCursor = cursor + currentIndent.length

    return TextFieldValue(
        text = finalText,
        selection = TextRange(finalCursor),
        composition = newValue.composition
    )
}

private fun findBracketIndices(textFieldValue: TextFieldValue): Set<Int> {
    val cursorPos = textFieldValue.selection.start
    val text = textFieldValue.text
    val brackets = setOf('(', ')', '{', '}', '[', ']')

    val bracketIndices = when {
        cursorPos < text.length && text[cursorPos] in brackets -> findBracketPairIndices(text, cursorPos)
        cursorPos > 0 && text[cursorPos - 1] in brackets -> findBracketPairIndices(text, cursorPos - 1)
        else -> emptySet()
    }
    return bracketIndices
}
