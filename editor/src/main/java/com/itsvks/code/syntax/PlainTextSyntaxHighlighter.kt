package com.itsvks.code.syntax

class PlainTextSyntaxHighlighter : SyntaxHighlighter {
    override fun highlight(text: String): List<Token> {
        return listOf(Token(TokenType.PLAIN, text.indices, text))
    }
}
