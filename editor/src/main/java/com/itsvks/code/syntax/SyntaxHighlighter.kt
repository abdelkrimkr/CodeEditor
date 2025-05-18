package com.itsvks.code.syntax

interface SyntaxHighlighter {
    fun highlight(text: String): List<Token>
}
