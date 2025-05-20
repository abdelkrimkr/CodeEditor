package com.itsvks.code.core

data class EditorSnapshot(
    val buffer: Rope,
    val cursorPosition: CursorPosition,
    val selectionRange: CursorRange?
)
