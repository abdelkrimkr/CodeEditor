package com.itsvks.code.language

object JavaScriptLanguage : Language {
    override val name = "JavaScript"
    override val fileExtensions = listOf("js", "jsx", "mjs", "cjs")

    override val keywords = setOf(
        "break", "case", "catch", "class", "const", "continue", "debugger", "default",
        "delete", "do", "else", "export", "extends", "false", "finally", "for", "function",
        "if", "import", "in", "instanceof", "new", "null", "return", "super", "switch",
        "this", "throw", "true", "try", "typeof", "var", "void", "while", "with", "yield",
        "let", "static", "enum", "await", "implements", "package", "protected", "interface",
        "private", "public", "async"
    )

    override val types = setOf(
        "Array", "ArrayBuffer", "Boolean", "DataView", "Date", "Error", "EvalError", "Float32Array",
        "Float64Array", "Function", "Int8Array", "Int16Array", "Int32Array", "Map", "Number",
        "Object", "Promise", "Proxy", "RangeError", "ReferenceError", "RegExp", "Set", "String",
        "Symbol", "SyntaxError", "TypeError", "Uint8Array", "Uint8ClampedArray", "Uint16Array",
        "Uint32Array", "URIError", "WeakMap", "WeakSet"
    )

    override val operators = setOf(
        "+", "-", "*", "/", "%", "**", "=", "+=", "-=", "*=", "/=", "%=", "**=",
        "++", "--", "==", "===", "!=", "!==", ">", "<", ">=", "<=", "&&", "||",
        "!", "?", ":", "??", "?.", "=>", "..."
    )

    override val punctuation = setOf(
        "(", ")", "[", "]", "{", "}", ",", ";", ".", "=>"
    )

    override val stringPatterns = listOf(
        "\"([^\"\\\\]|\\\\.)*\"".toRegex(),                   // Double-quoted strings
        "'([^'\\\\]|\\\\.)*'".toRegex(),                      // Single-quoted strings
        "`([^`\\\\]|\\\\.|\\\${[^}]*})*`".toRegex()           // Template literals
    )

    override val commentPatterns = listOf(
        "//.*".toRegex(),                                    // Single line comments
        "/\\*[\\s\\S]*?\\*/".toRegex()                       // Multi-line comments
    )

    override val functionPattern =
        "\\b(?:function\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)|([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*[=:]\\s*(?:async\\s+)?(?:function\\b|\\([^)]*\\)\\s*=>)|\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\()".toRegex()
}
