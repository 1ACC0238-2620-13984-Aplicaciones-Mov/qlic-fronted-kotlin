package com.qlic.mobile.feature.auth.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AndroidKeystoreTokenCipherTest {
    @Test
    fun tokenRoundTripsWithoutBeingStoredAsPlainText() {
        val cipher = AndroidKeystoreTokenCipher()

        val encrypted = cipher.encrypt("sensitive-jwt")

        assertFalse(encrypted.contains("sensitive-jwt"))
        assertEquals("sensitive-jwt", cipher.decrypt(encrypted))
    }
}
