package com.qlic.mobile.core.localization

enum class AppLanguage(
    val languageTag: String,
) {
    English("en"),
    SpanishLatinAmerica("es-419"),
    ;

    companion object {
        fun fromLanguageTag(languageTag: String?): AppLanguage = entries.firstOrNull {
            it.languageTag.equals(languageTag, ignoreCase = true)
        } ?: English
    }
}
