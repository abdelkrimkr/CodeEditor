package com.itsvks.code.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsvks.code.CodeEditorState
import com.itsvks.code.core.lineToAnnotatedString
import com.itsvks.code.core.rememberJetBrainsMonoFontFamily
import com.itsvks.code.syntax.SyntaxHighlighterFactory

@Composable
fun CodeEditor(
    state: CodeEditorState,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    softWrap: Boolean = false,
    gutterWidth: Dp = 48.dp,
    horizontalPadding: Dp = 8.dp
) {
    val fontFamily = rememberJetBrainsMonoFontFamily()
    val listState = rememberLazyListState()
    val rope = state.rope
    val syntaxHighlighter = remember(state.language) { SyntaxHighlighterFactory.createHighlighter(state.language) }

    val selectionColors = remember(state.theme) {
        TextSelectionColors(
            handleColor = state.theme.selectionHandleColor,
            backgroundColor = state.theme.selectionColor
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        SelectionContainer {
            CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (!softWrap) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                        .background(state.theme.backgroundColor),
                    state = listState
                ) {
                    items(rope.lineCount, key = { it }) { lineIdx ->
                        Row {
                            val lineStr = (lineIdx + 1).toString()

                            Box(
                                contentAlignment = Alignment.CenterEnd,
                                modifier = Modifier
                                    .width(gutterWidth)
                                    .background(state.theme.gutterBgColor)
                                    .padding(horizontal = horizontalPadding)
                            ) {
                                DisableSelection {
                                    Text(
                                        text = lineStr,
                                        color = state.theme.gutterTextColor,
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }

                            VerticalDivider(modifier = Modifier.fillMaxHeight())

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = horizontalPadding)
                            ) {
                                Text(
                                    text = rope.lineToAnnotatedString(lineIdx, state.theme, syntaxHighlighter),
                                    fontSize = fontSize,
                                    fontFamily = fontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
