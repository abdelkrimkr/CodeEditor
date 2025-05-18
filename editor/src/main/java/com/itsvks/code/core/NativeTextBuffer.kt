package com.itsvks.code.core

internal object NativeTextBuffer {
    init {
        System.loadLibrary("editor_native")
    }

    external fun createRopeFromFile(path: String): Long
    external fun createRope(text: String): Long
    external fun deleteRope(ropePtr: Long)
    external fun ropeLen(ropePtr: Long): Int
    external fun ropeLineCount(ropePtr: Long): Int
    external fun ropeInsert(ropePtr: Long, idx: Int, text: String)
    external fun ropeRemove(ropePtr: Long, start: Int, end: Int)
    external fun ropeSlice(ropePtr: Long, start: Int, end: Int): String
    external fun ropeLineToChar(ropePtr: Long, line: Int): Int
    external fun ropeCharToLine(ropePtr: Long, charIdx: Int): Int
    external fun ropeToString(ropePtr: Long): String
    external fun ropeGetChar(ropePtr: Long, index: Int): Char
    external fun ropeGetLine(ropePtr: Long, lineIndex: Int): String
    external fun ropeByteLen(ropePtr: Long): Int
    external fun ropeLineLen(ropePtr: Long, line: Int): Int
}
