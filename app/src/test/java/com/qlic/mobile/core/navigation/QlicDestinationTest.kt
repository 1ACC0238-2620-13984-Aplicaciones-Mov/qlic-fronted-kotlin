package com.qlic.mobile.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class QlicDestinationTest {
    @Test
    fun `primary destinations match the approved information architecture`() {
        assertEquals(
            listOf("dashboard", "alerts", "reports", "business", "more"),
            QlicDestination.primary.map(QlicDestination::route),
        )
    }

    @Test
    fun `secondary destinations stay under their approved sections`() {
        assertEquals(
            listOf("devices", "tanks"),
            QlicDestination.business.map(QlicDestination::route),
        )
        assertEquals(
            listOf("support", "account"),
            QlicDestination.more.map(QlicDestination::route),
        )
    }
}
