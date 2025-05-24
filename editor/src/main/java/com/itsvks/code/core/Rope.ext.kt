package com.itsvks.code.core

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

internal fun Rope.getLineText(lineIdx: Int) = line(lineIdx).toPlainString()

fun EmptyRope() = Rope.fromCharArray(CharArray(0), 0, 0)

