package com.genz.secure_share.algorithms

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.mockito.MockitoAnnotations
import javax.crypto.SecretKey

class AESTest {
    private lateinit var secretKey : SecretKey
    private var stringSecretKey : String? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        secretKey = AES.generateSecretKey(128)
        stringSecretKey = AES.secretKeyToString(secretKey)
    }

    @Test
    fun testGenerateSecretKeyForAES128() {
        val secretKey = AES.generateSecretKey(128)
        assertEquals(16, secretKey.encoded.size)
    }

    @Test
    fun testGenerateSecretKeyForAES192() {
        val secretKey = AES.generateSecretKey(192)
        assertEquals(24, secretKey.encoded.size)
    }

    @Test
    fun testGenerateSecretKeyForAES256() {
        val secretKey = AES.generateSecretKey(256)
        assertEquals(32, secretKey.encoded.size)
    }

    @Test
    fun testGenerateRandomIV() {
        val iv = AES.generateRandomIV()
        assertEquals(16, iv.size)
    }

    @Test
    fun testSecretKeyToStringNonNull() {
        val string = AES.secretKeyToString(secretKey)
        assertNotNull(string)
    }

    @Test
    fun testStringToSecretKeyNull() {
        val secret = AES.stringToSecretKey("invalid token")
        assertNull(secret)
    }

    @Test
    fun testStringToSecretKeyNotNull() {
        val secret = AES.stringToSecretKey(stringSecretKey?:"")
        assertNotNull(secret)
        assertEquals(secretKey, secret)
    }
}