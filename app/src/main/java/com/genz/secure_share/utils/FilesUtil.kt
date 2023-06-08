package com.genz.secure_share.utils

import android.content.Context
import android.net.Uri
import java.io.*

/**
 * Util class contains methods related to the read and write of the file,
 * read from file, write to the file
 */
class FilesUtil {
    /**
     * read file using uri
     *
     * @uri : Uri of the file to be read
     * @context : context of the activity it will be always MainActivity
     * @return the content of file in form of ByteArray
     * */
    fun readFile(context : Context, uri: Uri) : ByteArray {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val bufferedInputStream = BufferedInputStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()

        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }

        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Write Data to file
     */
    fun writeByteArrayToFile(context: Context, byteArray: ByteArray,
                                     fileName: String) : File {
        val file = File(context.filesDir, fileName)

        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(byteArray)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }
}