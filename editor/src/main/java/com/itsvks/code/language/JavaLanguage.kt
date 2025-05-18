package com.itsvks.code.language

object JavaLanguage : Language {
    override val name = "Java"
    override val fileExtensions = listOf("java")

    override val keywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"
    )

    override val types = setOf(
        "String", "Object", "Boolean", "Byte", "Character", "Double", "Float", "Integer",
        "Long", "Short", "StringBuilder", "StringBuffer", "List", "Map", "Set", "Collection",
        "ArrayList", "HashMap", "HashSet", "LinkedList", "TreeMap", "TreeSet", "Vector",
        "Iterable", "Iterator", "Comparable", "Throwable", "Exception", "Error", "Class"
    )

    override val operators = setOf(
        "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "++", "--",
        "&&", "||", "!", "==", "!=", ">", "<", ">=", "<=", "?:", "?", ":",
        ">>>", ">>", "<<", "&", "|", "^", "~", "&=", "|=", "^=", "<<=", ">>="
    )

    override val punctuation = setOf(
        "(", ")", "[", "]", "{", "}", ",", ";", ".", "::"
    )

    override val annotationPattern = "@[a-zA-Z_][a-zA-Z0-9_.]*".toRegex()

    override val staticPattern = "\\bstatic\\b".toRegex()
}
