package com.ragul.ccms.feature.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `valid email passes`() {
        assertNull(AuthValidator.validateEmail("ragul@example.com"))
        assertNull(AuthValidator.validateEmail("first.last+tag@sub.example.co.in"))
    }

    @Test
    fun `invalid email fails`() {
        assertEquals("Enter your email address.", AuthValidator.validateEmail(""))
        assertEquals("Enter your email address.", AuthValidator.validateEmail("   "))
        assertEquals("Enter a valid email address.", AuthValidator.validateEmail("not-an-email"))
        assertEquals("Enter a valid email address.", AuthValidator.validateEmail("a@b"))
    }

    @Test
    fun `login password only needs to be present`() {
        assertNull(AuthValidator.validatePassword("short"))
        assertEquals("Enter your password.", AuthValidator.validatePassword(""))
    }

    @Test
    fun `new password enforces length and character classes`() {
        assertNull(AuthValidator.validateNewPassword("Password1"))
        assertEquals(
            "Password must be at least 8 characters.",
            AuthValidator.validateNewPassword("Pass1"),
        )
        assertEquals(
            "Password must contain a letter.",
            AuthValidator.validateNewPassword("12345678"),
        )
        assertEquals(
            "Password must contain a number.",
            AuthValidator.validateNewPassword("abcdefgh"),
        )
    }

    @Test
    fun `password confirmation must match`() {
        assertNull(AuthValidator.validatePasswordConfirmation("Password1", "Password1"))
        assertEquals(
            "Passwords do not match.",
            AuthValidator.validatePasswordConfirmation("Password1", "Password2"),
        )
    }

    @Test
    fun `display name cannot be blank`() {
        assertNull(AuthValidator.validateName("Ragul"))
        assertEquals("Enter your name.", AuthValidator.validateName(" "))
    }

    @Test
    fun `user initial and friendly name fall back to email`() {
        val user = AuthUser(
            uid = "1",
            email = "ragul@example.com",
            displayName = null,
            photoUrl = null,
            isEmailVerified = false,
            provider = AuthProvider.PASSWORD,
        )

        assertEquals("R", user.initial)
        assertEquals("ragul", user.friendlyName)
    }
}
