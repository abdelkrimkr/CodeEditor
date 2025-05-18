package com.itsvks.code.syntax

enum class TokenType {
    PLAIN, KEYWORD, TYPE, STRING, COMMENT, NUMBER, OPERATOR, PUNCTUATION,
    FUNCTION, IDENTIFIER, ANNOTATION, MACRO, ATTRIBUTE, CONST, STATIC
}

data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int,
    val text: String? = null
) {
    val range: IntRange get() = start until end

    constructor(type: TokenType, range: IntRange, text: String? = null) : this(type, range.first, range.last + 1, text)
}
