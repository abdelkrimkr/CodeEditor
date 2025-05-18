package com.itsvks.code.language

object PlainTextLanguage : Language {
    override val name: String = "PlainText"

    override val fileExtensions: List<String> = listOf("txt", "text")

    override val keywords: Set<String> = emptySet()
    override val types: Set<String> = emptySet()
    override val operators: Set<String> = emptySet()
    override val punctuation: Set<String> = emptySet()

    override val stringPatterns: List<Regex> = emptyList()
    override val commentPatterns: List<Regex> = emptyList()

    override val numberPattern: Regex = Regex("(?!x)x")
    override val functionPattern: Regex? = null
    override val annotationPattern: Regex? = null
    override val macroPattern: Regex? = null
    override val attributePattern: Regex? = null
    override val constPattern: Regex? = null
    override val staticPattern: Regex? = null
}
