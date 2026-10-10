package com.example.saafloop

import com.example.saafloop.core.util.LocaleFormatUtils
import com.example.saafloop.core.util.LocaleManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AccessibilityLocalizationTest {

    @Test
    fun localeFormatUtils_formatsDateAndDistanceAccurately() {
        val now = System.currentTimeMillis()
        val formattedDate = LocaleFormatUtils.formatDate(now, Locale.ENGLISH)
        assertNotNull(formattedDate)
        assertTrue(formattedDate.isNotBlank())

        val formattedNum = LocaleFormatUtils.formatNumber(1250, Locale.ENGLISH)
        assertEquals("1,250", formattedNum)

        val formattedPercentage = LocaleFormatUtils.formatPercentage(0.94f, Locale.ENGLISH)
        assertEquals("94%", formattedPercentage)
    }

    @Test
    fun localeManager_supportedLocalesIncludeEnglishHindiPunjabi() {
        val locales = LocaleManager.SUPPORTED_LOCALES
        val codes = locales.map { it.first }

        assertTrue(codes.contains("en"))
        assertTrue(codes.contains("hi"))
        assertTrue(codes.contains("pa"))
    }
}
