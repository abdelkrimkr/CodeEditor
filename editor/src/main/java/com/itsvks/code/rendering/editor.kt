package com.itsvks.code.rendering

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.itsvks.code.core.TextBuffer
import com.itsvks.code.core.TextPosition
import com.itsvks.code.core.TextPositionRange
import com.itsvks.code.syntax.SyntaxHighlighter
import com.itsvks.code.theme.EditorTheme

internal fun DrawScope.drawSelection(
    from: TextPosition,
    to: TextPosition,
    lines: List<String>,
    lineHeight: Float,
    charWidth: Float,
    horizontalPadding: Float,
    color: Color
) {
    val startLine = from.line
    val endLine = to.line

    for (line in startLine .. endLine) {
        val x1 = when (line) {
            startLine -> from.column * charWidth + horizontalPadding
            else -> horizontalPadding
        }
        val x2 = when (line) {
            endLine -> to.column * charWidth + horizontalPadding
            else -> lines[line].length * charWidth + horizontalPadding
        }
        val y = line * lineHeight

        drawRect(
            color = color,
            topLeft = Offset(x1, y),
            size = Size(x2 - x1, lineHeight)
        )
    }
}

internal fun DrawScope.drawCursor(
    position: TextPosition,
    lineHeight: Float,
    charWidth: Float,
    horizontalPadding: Float,
    color: Color,
    width: Float = 2f,
    alpha: Float = 1f
) {
    val x = position.column * charWidth + horizontalPadding
    val y = position.line * lineHeight

    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(x, y),
        end = Offset(x, y + lineHeight),
        strokeWidth = width
    )
}

internal fun DrawScope.drawEditorContent(
    buffer: TextBuffer,
    syntaxHighlighter: SyntaxHighlighter,
    theme: EditorTheme,
    textPaint: Paint,
    lineHeight: Float,
    charWidth: Float,
    horizontalPadding: Float,
    cursorPosition: TextPosition,
    selectionRange: TextPositionRange?,
    cursorAlpha: Float,
    cursorWidth: Float,
    drawInvisibles: Boolean,
    firstVisibleLine: Int,
    lastVisibleLine: Int
) {
    val lines = buffer.lines
    val tokens = syntaxHighlighter.highlight(buffer.text)

    selectionRange?.let {
        drawSelection(
            from = it.start,
            to = it.endInclusive,
            lines = lines,
            lineHeight = lineHeight,
            charWidth = charWidth,
            horizontalPadding = horizontalPadding,
            color = theme.selectionColor
        )
    }

    val invisibleChars = mapOf(" " to '·', "\n" to '↴', "\t" to '→', "\r" to '↵')
    var tokenIndex = 0
    var globalCharIndex = 0

    lines.forEachIndexed { lineIndex, line ->
        if (lineIndex < firstVisibleLine || lineIndex > lastVisibleLine) {
            globalCharIndex += line.length + 1 // account for newline
            return@forEachIndexed
        }

        var xOffset = horizontalPadding

        line.forEachIndexed { charIndex, char ->
            val absoluteIndex = globalCharIndex + charIndex

            while (tokenIndex < tokens.lastIndex && absoluteIndex > tokens[tokenIndex].range.last) {
                tokenIndex++
            }

            val token = tokens.getOrNull(tokenIndex)
            val color = if (token != null && absoluteIndex in token.range) {
                theme.getColorForToken(token.type)
            } else {
                theme.defaultTextColor
            }

            textPaint.color = if (drawInvisibles && char.toString() in invisibleChars) {
                theme.invisibleCharColor.toArgb()
            } else {
                color.toArgb()
            }

            val charToDraw = if (drawInvisibles) invisibleChars[char.toString()] ?: char else char

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    /* text = */ charToDraw.toString(),
                    /* x = */ xOffset,
                    /* y = */ lineIndex * lineHeight + lineHeight * 0.8f,
                    /* paint = */ textPaint
                )
            }
            xOffset += charWidth
        }
        globalCharIndex += line.length + 1
    }

    drawCursor(
        position = cursorPosition,
        lineHeight = lineHeight,
        charWidth = charWidth,
        horizontalPadding = horizontalPadding,
        color = theme.cursorColor,
        width = cursorWidth,
        alpha = cursorAlpha
    )
}
