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
import com.itsvks.code.core.getLineSafe
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
    for (line in from.line..to.line) {
        val x1 = if (line == from.line) from.column * charWidth + horizontalPadding else horizontalPadding
        val x2 = if (line == to.line) to.column * charWidth + horizontalPadding else lines[line].length * charWidth + horizontalPadding
        val y = line * lineHeight
        drawRect(color = color, topLeft = Offset(x1, y), size = Size(x2 - x1, lineHeight))
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
    drawLine(color = color.copy(alpha = alpha), start = Offset(x, y), end = Offset(x, y + lineHeight), strokeWidth = width)
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
    val invisibleChars = mapOf(" " to '·', "\n" to '↴', "\t" to '→', "\r" to '↵')
    val lines = mutableListOf<String>()
    val lineOffsets = IntArray(lastVisibleLine - firstVisibleLine + 1)
    var offset = buffer.lineToChar(firstVisibleLine)
    for (i in firstVisibleLine..lastVisibleLine) {
        val line = buffer.getLineSafe(i).orEmpty()
        lines.add(line)
        lineOffsets[i - firstVisibleLine] = offset
        offset += line.length + 1
    }

    val tokens = syntaxHighlighter.highlight(
        buffer.slice(
            TextPosition(firstVisibleLine, 0),
            TextPosition(lastVisibleLine, buffer.getLineLength(lastVisibleLine))
        )
    )

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

    var tokenIndex = 0
    for (lineIndex in lines.indices) {
        val actualLine = firstVisibleLine + lineIndex
        val line = lines[lineIndex]
        var xOffset = horizontalPadding
        val baseCharIndex = lineOffsets[lineIndex]

        var charIndex = 0
        while (charIndex < line.length) {
            val absoluteIndex = baseCharIndex + charIndex
            while (tokenIndex < tokens.lastIndex && absoluteIndex > tokens[tokenIndex].range.last) {
                tokenIndex++
            }

            val token = tokens.getOrNull(tokenIndex)
            val color = if (token != null && absoluteIndex in token.range) theme.getColorForToken(token.type) else theme.defaultTextColor

            val char = line[charIndex]
            textPaint.color = if (drawInvisibles && char.toString() in invisibleChars) theme.invisibleCharColor.toArgb() else color.toArgb()
            val charToDraw = if (drawInvisibles) invisibleChars[char.toString()] ?: char else char

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(charToDraw.toString(), xOffset, actualLine * lineHeight + lineHeight * 0.8f, textPaint)
            }
            xOffset += charWidth
            charIndex++
        }
    }

    drawCursor(cursorPosition, lineHeight, charWidth, horizontalPadding, theme.cursorColor, cursorWidth, cursorAlpha)
}
