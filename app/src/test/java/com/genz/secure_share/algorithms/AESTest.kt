package com.genz.secure_share.algorithms

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import com.genz.secure_share.R
import com.genz.secure_share.utils.FilesUtil
import com.genz.secure_share.utils.NetworkCalls
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class AESTest {
    private lateinit var secretKey : SecretKey
    private var stringSecretKey : String? = null
    private lateinit var randomIv : ByteArray

    @Mock
    private lateinit var cipher: Cipher

    @Mock
    private lateinit var context : Context

    @Mock
    private lateinit var resources: Resources

    @Mock
    private lateinit var filePath : Uri

    @Mock
    private lateinit var file : File

    @Mock
    private lateinit var filesUtil: FilesUtil

    @Mock
    private lateinit var contentResolver: ContentResolver

    @Mock
    private lateinit var networkCalls: NetworkCalls

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        secretKey = AES.generateSecretKey(128)
        stringSecretKey = AES.secretKeyToString(secretKey)
        randomIv = AES.generateRandomIV()
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

    @Test
    fun encryptFileNonMediaFiles() {
        val fileData = "This file data".toByteArray()
        val cipherA = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipherA.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(randomIv))
        val encryptedData = cipherA.doFinal(fileData)

        Mockito.doReturn(resources).`when`(context).resources
        Mockito.`when`(context.resources.getStringArray(R.array.nonMediaFiles))
            .thenReturn(arrayOf(".txt"))
        Mockito.`when`(filesUtil.readFile(context, filePath)).thenReturn(fileData)
        Mockito.`when`(cipher.doFinal()).thenReturn(encryptedData)
        Mockito.`when`(filesUtil.writeByteArrayToFile(context, encryptedData,
            "demo.txt")).thenReturn(file)

        val res = AES.encryptFile(context, filePath, secretKey, "demo.txt",
            randomIv, filesUtil)

        assertNotNull(res)
        assertEquals(file, res)
        Mockito.verify(filesUtil).readFile(context, filePath)
        Mockito.verify(filesUtil).writeByteArrayToFile(context, encryptedData, "demo.txt")
    }

    @Test
    fun encryptFileMediaFiles() {
        val inputStream = AESTest::class.java.getResourceAsStream("/levi.png")

        Mockito.doReturn(resources).`when`(context).resources
        Mockito.`when`(context.resources.getStringArray(R.array.nonMediaFiles))
            .thenReturn(arrayOf(".txt"))
        Mockito.`when`(context.resources.getStringArray(R.array.mediaFiles))
            .thenReturn(arrayOf(".png"))
        Mockito.`when`(context.contentResolver).thenReturn(contentResolver)
        Mockito.`when`(context.contentResolver.openInputStream(filePath)).thenReturn(inputStream)

        val res = AES.encryptFile(context, filePath, secretKey, "demo.png",
            randomIv, filesUtil)

        assertNotNull(res)
        if (res != null) {
            assertEquals("demo.png", res.name)
        }
    }

    @Test
    fun testDecryptFile() {
        val fileData = "This file data".toByteArray()
        val cipherA = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipherA.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(randomIv))
        val encryptedData = cipherA.doFinal(fileData)

        Mockito.doReturn(resources).`when`(context).resources
        Mockito.`when`(context.resources.getStringArray(R.array.nonMediaFiles))
            .thenReturn(arrayOf(".txt"))
        Mockito.`when`(networkCalls.readRemoteFile("demoUrl.com")).thenReturn(encryptedData)
        Mockito.`when`(cipher.doFinal()).thenReturn(fileData)
        Mockito.`when`(filesUtil.writeByteArrayToFile(context, fileData, "demo.txt"))
            .thenReturn(file)

        val res = AES.decryptFile(context, "demoUrl.com", secretKey,
            "demo.txt", randomIv, filesUtil, networkCalls)

        assertNotNull(res)
        assertEquals(file, res)
        Mockito.verify(networkCalls).readRemoteFile("demoUrl.com")
        Mockito.verify(filesUtil).writeByteArrayToFile(context, fileData, "demo.txt")
    }
}