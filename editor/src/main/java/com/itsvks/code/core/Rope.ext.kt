package com.itsvks.code.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.itsvks.code.syntax.SyntaxHighlighter
import com.itsvks.code.theme.EditorTheme

internal fun Rope.lineToAnnotatedString(
    lineIdx: Int,
    theme: EditorTheme,
    syntaxHighlighter: SyntaxHighlighter
): AnnotatedString {
    val line = line(lineIdx).asCharSequence().trimEnd('\n', '\r')
    val tokens = syntaxHighlighter.highlight(line.toString())

    return buildAnnotatedString {
        tokens.fastForEach { token ->
            pushStyle(SpanStyle(color = theme.getColorForToken(token.type)))
            append(line.subSequence(token.start ..< token.end))
            pop()
//            pushStyle(SpanStyle(fontSize = 0.sp)) // Invisible newline
//            append("\n")
//            pop()
        }
    }
}

internal fun Rope.getLineAndCharOffset(globalOffset: Int): Pair<Int, Int> {
    require(globalOffset in 0 .. length) { "Offset $globalOffset out of bounds [0, $length]" }

    if (globalOffset == length) {
        return Pair(lineCount, line(lineCount).length)
    }

    var currentOffset = 0
    for (lineIdx in 0 .. lineCount) {
        val currentLine = line(lineIdx)
        val lineLengthWithNewline = currentLine.length

        if (globalOffset < currentOffset + lineLengthWithNewline) {
            return Pair(lineIdx, globalOffset - currentOffset)
        }

        currentOffset += lineLengthWithNewline
    }

    return Pair(lineCount, line(lineCount).length)
}

internal fun Rope.getGlobalOffset(lineIdx: Int, charOffsetInLine: Int): Int {
    require(lineIdx in 0 .. lineCount) { "Line index $lineIdx out of bounds [0, ${lineCount}]" }
    val line = line(lineIdx)
    require(charOffsetInLine in 0 .. line.length) {
        "Char offset $charOffsetInLine out of bounds [0, ${line.length}] for line $lineIdx"
    }

    val globalOffset = lineColumnToCharIndex(lineIdx, charOffsetInLine)
    return globalOffset
}

fun EmptyRope() = Rope.fromCharArray(CharArray(0), 0, 0)

