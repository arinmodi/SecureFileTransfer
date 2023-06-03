package com.example.video_share_app.algorithms

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
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
         * SecretKey to String conversion
         * @SecretKey : key to convert into string
         *
         * @return String form of the secret key
         */
        fun secretKeyToString(secretKey: SecretKey): String {
            val encodedKey = secretKey.encoded
            val base64Key = Base64.getEncoder().encodeToString(encodedKey)
            return String(base64Key.toByteArray(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
        }

        /**
         * Key String to SecretKey conversion
         * @keyString : key to convert into SecretKey
         *
         * @return SecretKey form of the given string
         */
        fun stringToSecretKey(keyString: String): SecretKey {
            val decodedKey = Base64.getDecoder().decode(keyString.toByteArray(
                StandardCharsets.UTF_8)
            )
            return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        }

        /**
         * filePath : Path of the file to be read
         * @return the content of file in form of ByteArray
         * */
         fun readFile(context : Context,uri: Uri) : ByteArray {
            val stringBuilder = StringBuilder()
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
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
         * filePath : Path of the file to be encrypted
         * secretKey : Secret Key for the encryption
         * @return String indicating the encrypted data
         */
        fun encryptFile(context: Context, filePath: Uri, secretKey: SecretKey, fileName: String) : File {
            val fileData = readFile(context, filePath)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            return writeByteArrayToFile(context, cipher.doFinal(fileData), fileName);
        }

        /**
         * filePath : Path of the file to be decrypted
         * secretKey : Secret Key for the decryption
         * @return String indicating the decrypted data
         */
        fun decryptFile(context: Context, filePath: Uri, secretKey: SecretKey) : String {
            val encryptedData = readFile(context, filePath)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            return cipher.doFinal(encryptedData).toString()
        }

        fun writeByteArrayToFile(context: Context, byteArray: ByteArray, fileName: String) : File {
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
}