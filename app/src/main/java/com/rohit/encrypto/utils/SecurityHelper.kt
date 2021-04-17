package com.rohit.encrypto.utils

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecurityHelper {
    private var secKeySpec: SecretKeySpec? = null
    private var cipher: Cipher? = null

    @Throws(Exception::class)
    fun initAESEncrypt(key: String?) {
        try {
            secKeySpec = SecretKeySpec(
                HexUtil.HexfromString(key),
                KEYFACTORY_AES
            )
            cipher =
                Cipher.getInstance(CIPHER_AES_TRANSFORMATION)
            cipher!!.init(
                1,
                secKeySpec,
                IvParameterSpec(ByteArray(16))
            )
        } catch (var3: NoSuchAlgorithmException) {
            throw Exception("Invalid Java Version")
        } catch (var4: NoSuchPaddingException) {
            throw Exception("Invalid Key")
        }
    }

    @Throws(Exception::class)
    fun initAESDecrypt(key: String?) {
        try {
            secKeySpec = SecretKeySpec(
                HexUtil.HexfromString(key),
                KEYFACTORY_AES
            )
            cipher = Cipher.getInstance(CIPHER_AES_TRANSFORMATION)
            cipher?.init(
                2,
                secKeySpec,
                IvParameterSpec(ByteArray(16))
            )
        } catch (var3: NoSuchAlgorithmException) {
            throw Exception("Invalid Java Version")
        } catch (var4: NoSuchPaddingException) {
            throw Exception("Invalid Key")
        }
    }

    @Throws(Exception::class)
    fun encryptAES(message: String, encryptionKey: String?): String {
        return try {
            initAESEncrypt(encryptionKey)
            val nse = cipher!!.doFinal(message.toByteArray())
            HexUtil.HextoString(nse)
        } catch (var4: BadPaddingException) {
            throw Exception("Invalid input String")
        }
    }

    @Throws(Exception::class)
    fun decryptAES(message: String?, decryptionKey: String?): String {
        return try {
            initAESDecrypt(decryptionKey)
            val nse = cipher!!.doFinal(
                HexUtil.HexfromString(message)
            )
            String(nse)
        } catch (var4: BadPaddingException) {
            throw Exception("Invalid input String")
        }
    }

    companion object {
        private const val KEYFACTORY_RSA = "RSA"
        private const val KEYFACTORY_AES = "AES"
        private const val CIPHER_RSA_TRANSFORMATION = "RSA/ECB/OAEPPadding"
        private const val CIPHER_AES_TRANSFORMATION = "AES/CTR/NoPadding"

        @Throws(NoSuchAlgorithmException::class)
        fun generateAESKey(): String {
            // Generate a 128-bit key
            val outputKeyLength = 128
            val secureRandom = SecureRandom()
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(outputKeyLength, secureRandom)
            val key = keyGenerator.generateKey()
            return HexUtil.asHex(key.encoded)
        }

        @Throws(Exception::class)
        fun getRSAPublicKey(publicKey: String?): PublicKey {
            val spec = X509EncodedKeySpec(
                Base64.decode(
                    publicKey,
                    Base64.DEFAULT
                )
            )
            val kf =
                KeyFactory.getInstance(KEYFACTORY_RSA)
            return kf.generatePublic(spec)
        }

        @Throws(Exception::class)
        fun getRSAPrivateKey(privateKey: String?): PrivateKey {
            val spec =
                PKCS8EncodedKeySpec(
                    Base64.decode(
                        privateKey,
                        Base64.DEFAULT
                    )
                )
            val kf =
                KeyFactory.getInstance(KEYFACTORY_RSA)
            return kf.generatePrivate(spec)
        }

        @Throws(Exception::class)
        fun encryptRSA(
            plainText: String,
            publicKey: PublicKey?
        ): String {
            val encryptCipher =
                Cipher.getInstance(CIPHER_RSA_TRANSFORMATION)
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val cipherText = encryptCipher.doFinal(plainText.toByteArray())
            return HexUtil.HextoString(cipherText)
        }
    }
}