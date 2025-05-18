package com.itsvks.code.language

// Registry of all supported languages
object LanguageRegistry {
    private val languages = mutableMapOf<String, Language>()
    private val extensionMap = mutableMapOf<String, Language>()

    init {
        register(KotlinLanguage)
        register(JavaLanguage)
        register(PythonLanguage)
        register(CppLanguage)
        register(JavaScriptLanguage)
        register(RustLanguage)
    }

    @JvmStatic
    fun register(language: Language) {
        languages[language.name.lowercase()] = language

        for (ext in language.fileExtensions) {
            extensionMap[ext.lowercase()] = language
        }
    }

    @JvmStatic
    fun getLanguageByName(name: String): Language? {
        return languages[name.lowercase()]
    }

    @JvmStatic
    fun getLanguageByExtension(extension: String): Language? {
        val ext = extension.lowercase().removePrefix(".")
        return extensionMap[ext]
    }

    @JvmStatic
    fun getLanguageByFileName(fileName: String): Language? {
        val lastDot = fileName.lastIndexOf('.')
        if (lastDot >= 0 && lastDot < fileName.length - 1) {
            val extension = fileName.substring(lastDot + 1).lowercase()
            return extensionMap[extension]
        }
        return null
    }

    @JvmStatic
    fun getAvailableLanguages(): List<String> = languages.keys.toList()
}
