package com.itsvks.code.language

object CppLanguage : Language {
    override val name = "C++"
    override val fileExtensions = listOf("cpp", "cc", "cxx", "c++", "h", "hpp", "hxx", "h++")

    override val keywords = setOf(
        "alignas", "alignof", "and", "and_eq", "asm", "atomic_cancel", "atomic_commit",
        "atomic_noexcept", "auto", "bitand", "bitor", "bool", "break", "case", "catch",
        "char", "char8_t", "char16_t", "char32_t", "class", "compl", "concept", "const",
        "consteval", "constexpr", "constinit", "const_cast", "continue", "co_await",
        "co_return", "co_yield", "decltype", "default", "delete", "do", "double",
        "dynamic_cast", "else", "enum", "explicit", "export", "extern", "false", "float",
        "for", "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace",
        "new", "noexcept", "not", "not_eq", "nullptr", "operator", "or", "or_eq", "private",
        "protected", "public", "register", "reinterpret_cast", "requires", "return",
        "short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct",
        "switch", "template", "this", "thread_local", "throw", "true", "try", "typedef",
        "typeid", "typename", "union", "unsigned", "using", "virtual", "void", "volatile",
        "wchar_t", "while", "xor", "xor_eq"
    )

    override val types = setOf(
        "string", "vector", "map", "set", "list", "deque", "queue", "stack", "array",
        "tuple", "pair", "optional", "variant", "any", "bitset", "valarray", "complex",
        "function", "shared_ptr", "unique_ptr", "weak_ptr", "thread", "mutex", "condition_variable",
        "future", "promise", "atomic", "exception", "stdexcept", "iostream", "stringstream"
    )

    override val operators = setOf(
        "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "++", "--",
        "&&", "||", "!", "==", "!=", ">", "<", ">=", "<=", "?", ":", "::", "->", ".",
        "->*", ".*", "&", "|", "^", "~", "<<", ">>", "<=>", "<=>"
    )

    override val punctuation = setOf(
        "(", ")", "[", "]", "{", "}", ",", ";", "#"
    )

    override val commentPatterns = listOf(
        "//.*".toRegex(),                   // Single line comments
        "/\\*[\\s\\S]*?\\*/".toRegex()      // Multi-line comments
    )

    override val macroPattern = "#[a-zA-Z_][a-zA-Z0-9_]*".toRegex()
}
