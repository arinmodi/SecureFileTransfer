package com.genz.secure_share.algorithms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.genz.secure_share.R
import com.genz.secure_share.utils.FilesUtil
import com.genz.secure_share.utils.NetworkCalls
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
    Contains methods for AES algorithm
 */
class AES {
    companion object {
        /**
         * Generate Random Pass for AES of the given KeySize
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
         * Generate Random IV for AES of the given KeySize
         *
         * @return ByteArray which will be used for encryption
         */
        fun generateRandomIV(): ByteArray {
            val iv = ByteArray(16)
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
        fun secretKeyToString(secretKey: SecretKey): String? {
            return try {
                val encodedKey = secretKey.encoded
                Base64.getEncoder().encodeToString(encodedKey)
            }catch (e : Exception) {
                Log.e("Error : ", "Secret to String")
                e.printStackTrace()
                null
            }
        }

        /**
         * Key String to SecretKey conversion
         * @keyString : key to convert into SecretKey
         *
         * @return SecretKey form of the given string
         */
        fun stringToSecretKey(keyString: String): SecretKey? {
            return try {
                val decodedKey = Base64.getDecoder().decode(keyString)
                SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
            } catch(e : Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Encrypt the File
         *
         * @context : context of the activity (Always MainActivity)
         * @filePath : Uri of the file to be encrypted
         * @secretKey : Secret Key for the encryption
         * @fileName : Name Of the output file
         * @iv : IV for AES CBC mode algo
         *
         * @return File indicating the encrypted file
         */
        fun encryptFile(context: Context, filePath: Uri, secretKey: SecretKey, fileName: String,
                        iv : ByteArray) : File? {
            try {
                val lastIndexOfDot = fileName.lastIndexOf(".")
                val extension = fileName.substring(lastIndexOfDot)
                Log.e("Extension", extension)
                return if (context.resources.getStringArray(R.array.nonMediaFiles)
                        .contains(extension)
                ) {
                    val fileData = FilesUtil.readFile(context, filePath)

                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

                    val encryptedData = cipher.doFinal(fileData)

                    FilesUtil.writeByteArrayToFile(context, encryptedData, fileName)
                } else if (context.resources.getStringArray(R.array.mediaFiles)
                        .contains(extension)
                ) {
                    return encryptMediaFile(context, filePath, fileName, secretKey, iv)
                } else {
                    null
                }
            } catch(e : Exception) {
                Log.e("Error : ", "Encryption Process")
                e.printStackTrace()
                return null
            }

        }

        /**
         * function for encrypting media files,
         * supported media files : .mp3, .mp4, .jpg, .png, .jpeg
         */
        private fun encryptMediaFile(context: Context, inputUri: Uri, outputFileName: String,
                                     secretKey: SecretKey, iv: ByteArray) : File {
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

        /**
         * Decrypt the File
         *
         * @context : context of the activity (Always Decryption Detail Activity)
         * @url : Url of the file to be decrypted
         * @secretKey : Secret Key for the decryption
         * @fileName : Name Of the output file
         * @iv : IV for AES CBC mode algo
         *
         * @return File indicating the decrypted file
         */
        fun decryptFile(context:Context, url : String, secretKey: SecretKey, fileName: String,
                        iv : ByteArray) : File? {
            val lastIndexOfDot = fileName.lastIndexOf(".")
            val extension = fileName.substring(lastIndexOfDot)
            Log.e("Extension", extension)
            if (context.resources.getStringArray(R.array.nonMediaFiles).contains(extension)
                || context.resources.getStringArray(R.array.mediaFiles).contains(extension)) {
                val encryptedData = NetworkCalls.readRemoteFile(url)

                return if (encryptedData != null) {
                    try {
                        Log.e("File Name : ", fileName)
                        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

                        val decryptedData = cipher.doFinal(encryptedData)

                        FilesUtil.writeByteArrayToFile(context, decryptedData, fileName)
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
    }
}