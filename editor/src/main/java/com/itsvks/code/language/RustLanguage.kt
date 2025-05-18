package com.itsvks.code.language

object RustLanguage : Language {
    override val name: String = "Rust"
    override val fileExtensions: List<String> = listOf("rs")

    override val keywords: Set<String> = setOf(
        "as", "break", "const", "continue", "crate", "else", "enum", "extern", "false", "fn", "for",
        "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref", "return",
        "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe", "use",
        "where", "while", "async", "await", "dyn"
    )

    override val types: Set<String> = setOf(
        "i8", "i16", "i32", "i64", "i128", "isize",
        "u8", "u16", "u32", "u64", "u128", "usize",
        "f32", "f64", "bool", "char", "str", "String", "Vec", "Option", "Result", "Box"
    )

    override val operators: Set<String> = setOf(
        "+", "-", "*", "/", "%", "=", "==", "!=", ">", "<", ">=", "<=", "&&", "||", "!", "&", "|", "^", "<<", ">>",
        "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", "->", "::", "=>"
    )

    override val punctuation: Set<String> = setOf(
        "{", "}", "[", "]", "(", ")", ",", ".", ";", ":"
    )

    override val stringPatterns: List<Regex>
        get() = listOf(
            "\"([^\"\\\\]|\\\\.)*\"".toRegex(), // regular strings
            "r#*\"[^\"]*\"#*".toRegex(),        // raw strings like r#"..."#
            "'([^'\\\\]|\\\\.)'".toRegex()      // char literals
        )

    override val commentPatterns: List<Regex>
        get() = listOf(
            "//.*".toRegex(),                    // single-line
            "/\\*([\\s\\S]*?)\\*/".toRegex()     // multi-line
        )

    override val macroPattern = "\\b[a-zA-Z_][a-zA-Z0-9_]*!\\b".toRegex()
    override val attributePattern = "#\\[.*?]".toRegex()
    override val staticPattern = "\\bstatic\\b".toRegex()
}
