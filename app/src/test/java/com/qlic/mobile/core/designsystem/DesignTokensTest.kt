package com.qlic.mobile.core.designsystem

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DesignTokensTest {
    @Test
    fun `primary color matches the approved Qlic blue`() {
        assertEquals(0xFF0C4AFD.toInt(), QlicPrimary.toArgb())
    }

    @Test
    fun `cards and touch targets use the approved dimensions`() {
        assertEquals(12f, QlicCardRadius.value)
        assertEquals(48f, QlicMinimumTouchTarget.value)
    }

    @Test
    fun `headings use the Qlic display font rather than the system body font`() {
        assertNotEquals(FontFamily.SansSerif, QlicTypography.headlineMedium.fontFamily)
    }
}
