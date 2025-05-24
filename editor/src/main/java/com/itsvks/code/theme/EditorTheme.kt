package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.fastForEach
import com.itsvks.code.core.Rope
import com.itsvks.code.syntax.SyntaxHighlighter
import com.itsvks.code.syntax.Token
import com.itsvks.code.syntax.TokenType

interface EditorTheme {
    val backgroundColor: Color
    val cursorColor: Color
    val selectionColor: Color
    val defaultTextColor: Color
    val gutterBgColor: Color
    val activeLineColor: Color get() = Color.Transparent
    val gutterTextColor: Color get() = defaultTextColor.copy(alpha = 0.6f)
    val gutterBorderColor: Color get() = Color.White
    val invisibleCharColor: Color get() = Color(0xFFAAAAAA)
    val selectionHandleColor: Color get() = cursorColor
    val scrollBarSelectedColor: Color get() = Color.White.copy(alpha = 0.5f)
    val scrollBarColor: Color get() = Color.LightGray.copy(alpha = 0.5f)

    fun getColorForToken(type: TokenType): Color
}

internal fun AnnotatedString.highlight(
    theme: EditorTheme,
    syntaxHighlighter: SyntaxHighlighter,
    bracketIndices: Set<Int> = emptySet()
) = buildAnnotatedString {
    val tokens = syntaxHighlighter.highlight(this@highlight.toString())
    highlightToken(tokens, theme, this@highlight, bracketIndices)
    //toAnnotatedString()
}

internal fun Rope.highlightLine(
    lineIdx: Int,
    theme: EditorTheme,
    syntaxHighlighter: SyntaxHighlighter,
    bracketIndices: Set<Int> = emptySet()
) = buildAnnotatedString {
    val line = line(lineIdx).asCharSequence().trimEnd('\n', '\r')
    val tokens = syntaxHighlighter.highlight(line.toString())

    highlightToken(tokens, theme, line, bracketIndices)
}

private fun AnnotatedString.Builder.highlightToken(
    tokens: List<Token>,
    theme: EditorTheme,
    line: CharSequence,
    bracketIndices: Set<Int>
) {
    tokens.fastForEach { token ->
        val color = theme.getColorForToken(token.type)
        val start = token.start
        val end = token.end

        for (i in start until end) {
            val char = line[i]
            val isBracket = i in bracketIndices
            val style = if (isBracket) {
                SpanStyle(
                    color = Color.Yellow, background = Color(0x33FFFFFF),
                    fontWeight = FontWeight.Bold
                )
            } else {
                SpanStyle(color = color)
            }
            pushStyle(style)
            append(char)
            pop()
        }
    }
}

internal fun findBracketPairIndices(code: String, cursor: Int): Set<Int> {
    val pairs = mapOf('(' to ')', '{' to '}', '[' to ']')
    val openToClose = pairs
    val closeToOpen = pairs.entries.associate { it.value to it.key }

    fun matchBracketAt(pos: Int): Set<Int> {
        if (pos < 0 || pos >= code.length) return emptySet()
        val char = code[pos]
        val stack = ArrayDeque<Int>()

        return when (char) {
            in openToClose -> {
                stack.addLast(pos)
                for (i in pos + 1 until code.length) {
                    when (code[i]) {
                        char -> stack.addLast(i)
                        openToClose[char] -> {
                            if (stack.size == 1) return setOf(pos, i)
                            stack.removeLast()
                        }
                    }
                }
                emptySet()
            }

            in closeToOpen -> {
                stack.addLast(pos)
                for (i in pos - 1 downTo 0) {
                    when (code[i]) {
                        char -> stack.addLast(i)
                        closeToOpen[char] -> {
                            if (stack.size == 1) return setOf(i, pos)
                            stack.removeLast()
                        }
                    }
                }
                emptySet()
            }

            else -> emptySet()
        }
    }

    // try current cursor position
    val direct = matchBracketAt(cursor)
    if (direct.isNotEmpty()) return direct

    // try adjacent positions (left and right)
    val left = matchBracketAt(cursor - 1)
    if (left.isNotEmpty()) return left

    val right = matchBracketAt(cursor + 1)
    if (right.isNotEmpty()) return right

    return emptySet()
}
