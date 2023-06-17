package com.genz.secure_share.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class FilesUtilTest {
    @Mock
    private lateinit var context : Context

    @Mock
    private lateinit var contentResolver: ContentResolver

    @Mock
    private lateinit var uri : Uri

    @Mock
    private lateinit var file : File

    private lateinit var inputStream: InputStream

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        inputStream = ByteArrayInputStream("test data".toByteArray())
    }

    @Test
    fun testReadFile() {
        Mockito.`when`(context.contentResolver).thenReturn(contentResolver)
        Mockito.`when`(contentResolver.openInputStream(uri)).thenReturn(inputStream)

        val files = FilesUtil()
        val bytes = files.readFile(context, uri)
        val length = bytes.isNotEmpty()

        assertEquals(true, length)
        assertEquals("test data", bytes.toString(Charsets.UTF_8))
    }

    @Test
    fun testWriteByteArrayToFile() {
        val fileName = "demo.txt"
        val byteArray = "test data".toByteArray()
        val resultFile = FilesUtil().writeByteArrayToFile(context, byteArray, fileName)

        assertEquals("demo.txt", resultFile.name)
    }
}