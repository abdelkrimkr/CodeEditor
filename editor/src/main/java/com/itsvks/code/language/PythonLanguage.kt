package com.itsvks.code.language

object PythonLanguage : Language {
    override val name = "Python"
    override val fileExtensions = listOf("py", "pyw", "pyc", "pyo", "pyd")

    override val keywords = setOf(
        "False", "None", "True", "and", "as", "assert", "async", "await", "break",
        "class", "continue", "def", "del", "elif", "else", "except", "finally",
        "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
        "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
    )

    override val types = setOf(
        "bool", "bytearray", "bytes", "complex", "dict", "float", "frozenset", "int",
        "list", "memoryview", "object", "range", "set", "slice", "str", "tuple", "type"
    )

    override val operators = setOf(
        "+", "-", "*", "/", "//", "%", "**", "=", "+=", "-=", "*=", "/=", "//=", "%=", "**=",
        "==", "!=", ">", "<", ">=", "<=", "and", "or", "not", "is", "is not", "in", "not in"
    )

    override val punctuation = setOf(
        "(", ")", "[", "]", "{", "}", ",", ":", ".", "@", "=", "->"
    )

    override val stringPatterns = listOf(
        "\"\"\"[\\s\\S]*?\"\"\"".toRegex(),   // Triple double-quoted strings
        "'''[\\s\\S]*?'''".toRegex(),        // Triple single-quoted strings
        "\"([^\"\\\\]|\\\\.)*\"".toRegex(),   // Double-quoted strings
        "'([^'\\\\]|\\\\.)*'".toRegex(),      // Single-quoted strings
        "r\"([^\"\\\\]|\\\\.)*\"".toRegex(),  // Raw double-quoted strings
        "r'([^'\\\\]|\\\\.)*'".toRegex(),     // Raw single-quoted strings
        "f\"([^\"\\\\]|\\\\.)*\"".toRegex(),  // Formatted double-quoted strings
        "f'([^'\\\\]|\\\\.)*'".toRegex()      // Formatted single-quoted strings
    )

    override val commentPatterns = listOf(
        "#.*".toRegex()                    // Single line comments
    )

    override val functionPattern = "\\bdef\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(".toRegex()

    override val annotationPattern = "@[a-zA-Z_][a-zA-Z0-9_.]*".toRegex()
}
