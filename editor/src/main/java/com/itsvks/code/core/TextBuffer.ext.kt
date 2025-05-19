package com.itsvks.code.core

fun TextBuffer.getLineSafe(index: Int, default: String? = ""): String? {
    return if (index in 0 until lineCount) getLine(index) else default
}

fun TextBuffer.insertLine(index: Int, text: String) {
    insertText(TextPosition(index, 0), text + "\n")
}

fun TextBuffer.deleteLine(index: Int) {
    val line = getLineSafe(index) ?: return

    deleteRange(
        TextPosition(index, 0),
        TextPosition(index, line.length + 1) // Include newline
    )
}

fun TextBuffer.swapLines(index1: Int, index2: Int) {
    if (index1 !in 0 until lineCount || index2 !in 0 until lineCount) return
    val line1 = getLine(index1)
    val line2 = getLine(index2)
    deleteLine(index1)
    insertLine(index1, line2)
    deleteLine(index2)
    insertLine(index2, line1)
}
