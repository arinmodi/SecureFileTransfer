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
            val stringBuilder = StringBuilder()
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    stringBuilder.append("\n")
                    line = reader.readLine()
                }
                reader.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return stringBuilder.toString().toByteArray()
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
                        iv : ByteArray) : File {
            val fileData = readFile(context, filePath)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

            val encryptedData = cipher.doFinal(fileData)

            return writeByteArrayToFile(context, encryptedData, fileName)
        }

        /**
         * filePath : Path of the file to be decrypted
         * secretKey : Secret Key for the decryption
         * @return String indicating the decrypted data
         */
        fun decryptFile(context:Context, url : String, secretKey: SecretKey, fileName: String,
                        iv : ByteArray) : File? {
            val encryptedData = readRemoteFile(url)

            return if (encryptedData != null) {
                try {
                    Log.e("File Name : ", fileName)
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

                    val decryptedData = cipher.doFinal(encryptedData)

                    writeByteArrayToFileWithoutEncoding(context, decryptedData, fileName)
                }catch(e : Exception) {
                    Log.e("Error : ", "Decryption")
                    e.printStackTrace()
                    return null
                }
            } else {
                null
            }
        }

        private fun writeByteArrayToFileWithoutEncoding(context: Context, byteArray: ByteArray, fileName: String) : File {
            val file = File(context.filesDir, fileName)

            try {
                val outputStream = FileOutputStream(file)
                outputStream.write(byteArray)
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return file;
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

        fun encryptMediaFile(context: Context, inputUri: Uri, outputFileName: String, secretKey: SecretKey,
                             iv: ByteArray) : File {
            val inputStream = context.contentResolver.openInputStream(inputUri)
            val file = File(context.filesDir, outputFileName)
            val outputStream = FileOutputStream(file)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

            val cipherOutputStream = CipherOutputStream(outputStream, cipher)

            inputStream?.use { input ->
                cipherOutputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return file
        }

        fun decryptMediaFile(context: Context, inputFileName: String, outputUri: Uri, secretKey: SecretKey,
                             iv: ByteArray) {
            val inputStream = FileInputStream(File(context.filesDir, inputFileName))
            val outputStream = context.contentResolver.openOutputStream(outputUri)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

            val cipherInputStream = CipherInputStream(inputStream, cipher)

            outputStream?.use { output ->
                cipherInputStream.use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
}