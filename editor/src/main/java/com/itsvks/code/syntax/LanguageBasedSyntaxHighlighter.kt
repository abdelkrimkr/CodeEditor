package com.itsvks.code.syntax

import com.itsvks.code.language.Language

// Implementation of the SyntaxHighlighter that uses Language definitions
class LanguageBasedSyntaxHighlighter(private val language: Language) : SyntaxHighlighter {

    private val keywordPattern: Regex? = language.keywords
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|", prefix = "\b(?:", postfix = ")\b")
        ?.toRegex()

    private val typePattern: Regex? = language.types
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|", prefix = "\b(?:", postfix = ")\b")
        ?.toRegex()

    private val operatorPattern: Regex? = language.operators
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|") { Regex.escape(it) }
        ?.toRegex()

    private val punctuationPattern: Regex? = language.punctuation
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|") { Regex.escape(it) }
        ?.toRegex()

    override fun highlight(text: String): List<Token> {
        val tokens = mutableListOf<Token>()

        findAndAddTokens(text, language.commentPatterns, TokenType.COMMENT, tokens)
        findAndAddTokens(text, language.stringPatterns, TokenType.STRING, tokens)
        findAndAddPattern(text, language.numberPattern, TokenType.NUMBER, tokens)

        val functionPattern = language.functionPattern
        if (functionPattern != null) {
            val results = functionPattern.findAll(text)
            for (result in results) {
                val matchText = result.groupValues[1]
                if (!isInExistingToken(result.range.first, tokens)) {
                    tokens.add(Token(
                        TokenType.FUNCTION,
                        result.range.first,
                        result.range.first + matchText.length,
                        matchText
                    ))
                }
            }
        }

        language.annotationPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.ANNOTATION, tokens)
        }

        language.macroPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.MACRO, tokens)
        }

        language.attributePattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.ATTRIBUTE, tokens)
        }

        language.constPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.CONST, tokens)
        }

        language.staticPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.STATIC, tokens)
        }

        keywordPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.KEYWORD, tokens)
        }

        typePattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.TYPE, tokens)
        }

        operatorPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.OPERATOR, tokens)
        }

        punctuationPattern?.let { pattern ->
            findAndAddPattern(text, pattern, TokenType.PUNCTUATION, tokens)
        }

        val identifierPattern = "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b".toRegex()
        val results = identifierPattern.findAll(text)
        for (result in results) {
            if (!isInExistingToken(result.range.first, tokens)) {
                tokens.add(Token(TokenType.IDENTIFIER, result.range, result.value))
            }
        }

        // Any text not covered by the above tokens gets TokenType.PLAIN
        val coveredRanges = tokens.map { it.range }.sortedBy { it.first }
        var lastEnd = 0

        for (range in coveredRanges) {
            if (range.first > lastEnd) {
                tokens.add(Token(TokenType.PLAIN, lastEnd, range.first, text.substring(lastEnd, range.first)))
            }
            lastEnd = range.last + 1
        }

        if (lastEnd < text.length) {
            tokens.add(Token(TokenType.PLAIN, lastEnd, text.length, text.substring(lastEnd)))
        }

        return tokens.sortedBy { it.start }
    }

    private fun findAndAddTokens(text: String, patterns: List<Regex>, type: TokenType, tokens: MutableList<Token>) {
        for (pattern in patterns) {
            val results = pattern.findAll(text)
            for (result in results) {
                if (!isInExistingToken(result.range.first, tokens)) {
                    tokens.add(Token(type, result.range, result.value))
                }
            }
        }
    }

    private fun findAndAddPattern(text: String, pattern: Regex, type: TokenType, tokens: MutableList<Token>) {
        val results = pattern.findAll(text)
        for (result in results) {
            if (!isInExistingToken(result.range.first, tokens)) {
                tokens.add(Token(type, result.range, result.value))
            }
        }
    }

    private fun isInExistingToken(position: Int, tokens: List<Token>): Boolean {
        return tokens.any { position in it.range }
    }
}
