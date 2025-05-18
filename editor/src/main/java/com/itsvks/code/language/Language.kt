package com.itsvks.code.language

interface Language {
    val name: String
    val fileExtensions: List<String>

    // Language specific patterns
    val keywords: Set<String>
    val types: Set<String>
    val operators: Set<String>
    val punctuation: Set<String>

    // Optional language-specific patterns (can be overridden by specific languages)
    val stringPatterns: List<Regex>
        get() = listOf(
            "\"([^\"\\\\]|\\\\.)*\"".toRegex(),
            "'([^'\\\\]|\\\\.)*'".toRegex()
        )
    val commentPatterns: List<Regex>
        get() = listOf(
            "//.*".toRegex(),
            "/\\*[\\s\\S]*?\\*/".toRegex()
        )

    val numberPattern: Regex get() = "\\b(\\d+(\\.\\d+)?([eE][+-]?\\d+)?|0x[0-9a-fA-F]+)\\b".toRegex()
    val functionPattern: Regex? get() = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(".toRegex()
    val annotationPattern: Regex? get() = "@[a-zA-Z_][a-zA-Z0-9_]*".toRegex()
    val macroPattern: Regex? get() = null
    val attributePattern: Regex? get() = null
    val constPattern: Regex? get() = "\\b[A-Z][A-Z0-9_]*\\b".toRegex()
    val staticPattern: Regex? get() = null
}
