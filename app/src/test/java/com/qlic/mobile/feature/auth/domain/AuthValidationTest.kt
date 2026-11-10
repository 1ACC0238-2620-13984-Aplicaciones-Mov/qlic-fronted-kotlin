package com.qlic.mobile.feature.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {
    @Test
    fun `email must use a valid address format`() {
        assertTrue(AuthValidator.isEmailValid("person@company.com"))
        assertFalse(AuthValidator.isEmailValid("person@"))
        assertFalse(AuthValidator.isEmailValid(" person@company.com "))
    }

    @Test
    fun `password accepts between 8 and 72 characters`() {
        assertFalse(AuthValidator.isPasswordValid("1234567"))
        assertTrue(AuthValidator.isPasswordValid("12345678"))
        assertTrue(AuthValidator.isPasswordValid("a".repeat(72)))
        assertFalse(AuthValidator.isPasswordValid("a".repeat(73)))
    }

    @Test
    fun `registration validates name email password and terms in order`() {
        assertEquals(
            AuthError.InvalidName,
            AuthValidator.validate(SignUpRequest("", "bad", "short", acceptedTerms = false)),
        )
        assertEquals(
            AuthError.InvalidEmail,
            AuthValidator.validate(SignUpRequest("Ana", "bad", "short", acceptedTerms = false)),
        )
        assertEquals(
            AuthError.InvalidPassword,
            AuthValidator.validate(SignUpRequest("Ana", "ana@qlic.com", "short", acceptedTerms = false)),
        )
        assertEquals(
            AuthError.TermsRequired,
            AuthValidator.validate(SignUpRequest("Ana", "ana@qlic.com", "valid-pass", acceptedTerms = false)),
        )
    }
}
