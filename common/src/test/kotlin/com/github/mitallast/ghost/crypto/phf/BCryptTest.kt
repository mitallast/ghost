package com.github.mitallast.ghost.crypto.phf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BCryptTest {

    @Test
    fun hash() {

        val salt = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

        assertNotNull(BCrypt.hash("password"))
        assertEquals(60, BCrypt.hash("password").length)

        assertNotNull(BCrypt.hash("password", 12))
        assertEquals(60, BCrypt.hash("password", 12).length)

        assertNotNull(BCrypt.hash("password", 10, salt))
        assertEquals(60, BCrypt.hash("password", 10, salt).length)
        assertEquals("\$2a\$10\$.OGB/.SE/ueHAeqKBO2NC.YH6Fqo3pzbCQM59uq8aCSem6kZXcPHe", BCrypt.hash("password", 10, salt))
        assertEquals("\$2a\$12\$.OGB/.SE/ueHAeqKBO2NC.9VsFtzpHwWDllsNgNKuufkIEUy6HN6q", BCrypt.hash("password", 12, salt))
    }

    @Test
    fun verify() {

        val salt = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

        assertTrue(BCrypt.verify("password", BCrypt.hash("password")))
        assertTrue(BCrypt.verify("password", BCrypt.hash("password", 9)))
        assertTrue(BCrypt.verify("password", BCrypt.hash("password", 12, salt)))

        assertTrue(BCrypt.verify("A quick movement of the enemy will jeopardize six gunboats.", "\$2a\$09\$XR8g3EKqRHFe5Xk/xXy54.RJDJCb496gDC9OjMPKXSiOoI0H6MhBW"))
        assertTrue(BCrypt.verify("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.", "\$2a\$12\$fGaRVWO1awxzaJIeLGVjCu9gyPMIW3UYUerGYSueFISMCOwzdMjpm"))
    }
}