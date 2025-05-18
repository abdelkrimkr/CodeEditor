package com.itsvks.code.syntax

import com.itsvks.code.language.Language
import com.itsvks.code.language.LanguageRegistry

object SyntaxHighlighterFactory {
    @JvmStatic
    fun createHighlighter(language: Language): SyntaxHighlighter {
        return LanguageBasedSyntaxHighlighter(language)
    }

    @JvmStatic
    fun createHighlighter(languageName: String): SyntaxHighlighter? {
        val language = LanguageRegistry.getLanguageByName(languageName) ?: return null
        return LanguageBasedSyntaxHighlighter(language)
    }

    @JvmStatic
    fun createHighlighterForFile(fileName: String): SyntaxHighlighter? {
        val language = LanguageRegistry.getLanguageByFileName(fileName) ?: return null
        return LanguageBasedSyntaxHighlighter(language)
    }
}
