package com.genz.secure_share.utils

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Contains the method which works with remote resources
 * read from remote file
 */
class NetworkCalls {
    /**
     * Read the data from remote file
     *
     * @url : url of the file to be read
     * @return the content of the file in form of ByteArray
     */
    fun readRemoteFile(urlString: String): ByteArray? {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            val inputStream = BufferedInputStream(connection.inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()

            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }

            val encodedData = byteArrayOutputStream.toByteArray()
            inputStream.close()
            byteArrayOutputStream.close()

            return encodedData
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}