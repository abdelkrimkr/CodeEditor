package com.itsvks.code.language

object KotlinLanguage : Language {
    override val name = "Kotlin"
    override val fileExtensions = listOf("kt", "kts")

    override val keywords = setOf(
        "abstract", "actual", "annotation", "as", "break", "by", "catch", "class", "companion",
        "const", "constructor", "continue", "crossinline", "data", "do", "dynamic", "else",
        "enum", "expect", "external", "false", "final", "finally", "for", "fun", "get", "if",
        "import", "in", "infix", "init", "inline", "inner", "interface", "internal", "is", "lateinit",
        "noinline", "null", "object", "open", "operator", "out", "override", "package", "private",
        "protected", "public", "reified", "return", "sealed", "set", "super", "suspend", "tailrec",
        "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "vararg", "when",
        "where", "while"
    )

    override val types = setOf(
        "Any", "Unit", "String", "Array", "Int", "Boolean", "Char", "Long", "Double",
        "Float", "Short", "Byte", "Nothing", "List", "Map", "Set", "Pair", "Triple",
        "Collection", "Sequence", "Iterator", "Iterable", "MutableList", "MutableMap",
        "MutableSet", "Comparable", "Throwable", "Exception", "Error"
    )

    override val operators = setOf(
        "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "++", "--",
        "&&", "||", "!", "==", "!=", ">", "<", ">=", "<=", "?:", "?.", "?", ":",
        "..", "->", "=>"
    )

    override val punctuation = setOf(
        "(", ")", "[", "]", "{", "}", ",", ";", ".", "::", "@"
    )

    override val stringPatterns = listOf(
        "\"\"\"[\\s\\S]*?\"\"\"".toRegex(),  // Triple-quoted strings
        "\"([^\"\\\\]|\\\\.)*\"".toRegex(),  // Double-quoted strings
        "'[^']'".toRegex(),                  // Character literals
    )

    override val commentPatterns = listOf(
        "//.*".toRegex(),                   // Single line comments
        "/\\*[\\s\\S]*?\\*/".toRegex()      // Multi-line comments
    )

    override val annotationPattern = "@[a-zA-Z_][a-zA-Z0-9_.]*".toRegex()
}
