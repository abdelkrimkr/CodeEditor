package com.itsvks.code.core

data class EditorSnapshot(
    val buffer: TextBuffer,
    val cursorPosition: TextPosition,
    val selectionRange: TextPositionRange?
)
