package com.example.saafloop.core.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Manages in-app language selection (English, Hindi, Punjabi) and locale configuration.
 */
object LocaleManager {

    val SUPPORTED_LOCALES = listOf(
        Pair("en", "English"),
        Pair("hi", "हिंदी (Hindi)"),
        Pair("pa", "ਪੰਜਾਬੀ (Punjabi)")
    )

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
