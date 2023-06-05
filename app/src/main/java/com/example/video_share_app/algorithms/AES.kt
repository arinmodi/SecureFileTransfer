package com.example.video_share_app.algorithms

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/*
    Contains methods for AES algorithm
 */
class AES {
    companion object {
        /**
         * Generate Random Key for AES of the given KeySize
         *
         * @keySize : key size can be 128 bit, 256 bit or 512 bit
         * @return SecretKey which will be used for encryption
         */
         fun generateSecretKey(keySize : Int): SecretKey {
            val keyGen = KeyGenerator.getInstance("AES")
            val secureRandom = SecureRandom()
            keyGen.init(keySize, secureRandom)
            return keyGen.generateKey()
         }

        /**
         * Generate Random IV
         */
        fun generateRandomIV(): ByteArray {
            val iv = ByteArray(16) // IV size for AES is 16 bytes (128 bits)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(iv)
            return iv
        }

        /**
         * SecretKey to String conversion
         * @SecretKey : key to convert into string
         *
         * @return String form of the secret key
         */
        fun secretKeyToString(secretKey: SecretKey): String {
            val encodedKey = secretKey.encoded
            return Base64.getEncoder().encodeToString(encodedKey)
        }

        /**
         * Key String to SecretKey conversion
         * @keyString : key to convert into SecretKey
         *
         * @return SecretKey form of the given string
         */
        fun stringToSecretKey(keyString: String): SecretKey {
            val decodedKey = Base64.getDecoder().decode(keyString)
            return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        }

        /**
         * filePath : Path of the file to be read
         * @return the content of file in form of ByteArray
         * */
        private fun readFile(context : Context,uri: Uri) : ByteArray {
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
         * url : Read the data from remote file
         * @return the content of the file in form of ByteArray
         */
        private fun readRemoteFile(urlString : String) : ByteArray? {
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

        /**
         * filePath : Path of the file to be encrypted
         * secretKey : Secret Key for the encryption
         * @return String indicating the encrypted data
         */
        fun encryptFile(context: Context, filePath: Uri, secretKey: SecretKey, fileName: String,
                        iv : ByteArray) : File? {
            return if (fileName.contains(".txt") || fileName.contains(".pdf")) {
                val fileData = readFile(context, filePath)

                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

                val encryptedData = cipher.doFinal(fileData)

                writeByteArrayToFile(context, encryptedData, fileName)
            } else if (fileName.contains(".jpg") || fileName.contains(".png")
                || fileName.contains(".mp3") || fileName.contains(".png")
                || fileName.contains(".mp4")) {
                return encryptMediaFile(context, filePath, fileName, secretKey, iv)
            }else {
                null
            }
        }

        /**
         * filePath : Path of the file to be decrypted
         * secretKey : Secret Key for the decryption
         * @return String indicating the decrypted data
         */
        fun decryptFile(context:Context, url : String, secretKey: SecretKey, fileName: String,
                        iv : ByteArray) : File? {

            if (fileName.contains(".txt") ||
                    fileName.contains(".jpeg") || fileName.contains(".jpg")
                || fileName.contains(".png") || fileName.contains(".mp4")
                || fileName.contains(".mp3") || fileName.contains(".pdf")) {
                val encryptedData = readRemoteFile(url)

                return if (encryptedData != null) {
                    try {
                        Log.e("File Name : ", fileName)
                        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

                        val decryptedData = cipher.doFinal(encryptedData)

                        writeByteArrayToFile(context, decryptedData, fileName)
                    } catch (e: Exception) {
                        Log.e("Error : ", "Decryption")
                        e.printStackTrace()
                        return null
                    }
                } else {
                    null
                }
            }else {
                return null
            }
        }

        private fun writeByteArrayToFile(context: Context, byteArray: ByteArray, fileName: String) : File {
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

        /**
         * function for encrypting media files,
         * supported media files : .mp3, .mp4, .jpg, .png, .jpeg
         */
        private fun encryptMediaFile(context: Context, inputUri: Uri, outputFileName: String, secretKey: SecretKey,
                                     iv: ByteArray) : File {
            val inputStream = context.contentResolver.openInputStream(inputUri)
            val file = File(context.filesDir, outputFileName)
            val outputStream = FileOutputStream(file)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

            val cipherOutputStream = CipherOutputStream(outputStream, cipher)

            inputStream?.use { input ->
                cipherOutputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return file
        }
    }
}