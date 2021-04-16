package com.rohit.encrypto.encryption_decryption

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class EncAndDecUtil {
    companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val ALIAS = "Encrypto"
        const val TAG = "KeyStoreManager"
    }

    private var keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE)

    init {
        keyStore.load(null)
    }

    fun encryptString(text: String): SecuredData {
        return try {
            val result = encryptData(text)
            if (result != null) {
                SecuredData(result.first.toString(Charsets.ISO_8859_1), result.second)
            } else {
                SecuredData("Error", "Error")
            }
        } catch (e: Exception) {
            Log.e(TAG,"error encrypted string", e)
            SecuredData("Error", "Error")
        }
    }

    private fun encryptData(text: String): Pair<ByteArray, String>? {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKet(ALIAS))
            val iv = cipher.iv
            val result = cipher.doFinal(text.toByteArray(Charsets.ISO_8859_1))
            val resultIv = Base64.encodeToString(iv, Base64.NO_WRAP)
            Log.i(TAG,"encrypted data $result")
            Log.i(TAG,"encrypted iv $iv")
            return if (result != null) {
                Pair(result, resultIv)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG,"error encryptData", e)
            return null
        }
    }

    /**
    Get pair of encrypted value and iv
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun decryptString(encryptedString: String, iv: String): String {
        return try {
            val result = decryptData(
                encryptedString.toByteArray(Charsets.ISO_8859_1),
                Base64.decode(iv, Base64.NO_WRAP)
            )
            result
        } catch (e: java.lang.Exception) {
            Log.e(TAG,"Error in convert to Base64")
            encryptedString
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun decryptData(encryptedData: ByteArray, iv: ByteArray): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKet(ALIAS), spec)
            val result = cipher.doFinal(encryptedData).toString(Charsets.ISO_8859_1)
            Log.i(TAG,"decrypted data $result")
            result
        } catch (e: Exception) {
            Log.e(TAG,"decryptData error may string was not encrypted", e)
            encryptedData.toString()
        }
    }

    private fun getSecretKet(alias: String): Key {
        if (keyStore.containsAlias(alias)) {
            // Try for existing key
            return keyStore.getKey(alias, null)
        } else {
            // Key is not present, create new one.
            val keyGenerator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val kGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                val specs = KeyGenParameterSpec
                    .Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                kGenerator.init(specs)
                kGenerator
            } else {
                KeyGenerator.getInstance(ANDROID_KEY_STORE)
            }
            return keyGenerator.generateKey()
        }
    }

    @Keep
    data class SecuredData(val value: String, val encryptedValue: String)
}