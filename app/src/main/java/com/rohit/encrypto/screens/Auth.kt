package com.rohit.encrypto.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.rohit.encrypto.R
import java.net.Authenticator
import java.util.concurrent.Executor


class Auth : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        executor = ContextCompat.getMainExecutor(this)
        biometricManager = BiometricManager.from(this)
        authCheck()
    }

    private fun authCheck() {
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> authUser(executor)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> showToast("No biometric hardware")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> showToast("Biometric hardware unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> showToast("Biometric not setup")
        }
    }

    private fun authUser(executor: Executor) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Encrypto")
            .setSubtitle("Authentication Required!")
            .setDescription("Please Authenticate to be able to view your notes")
            .setDeviceCredentialAllowed(true)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    showToast("Authentication Pass")
                    startActivity(Intent(this@Auth, MainActivity::class.java))
                }

                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast("Authentication Fail")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                }
            }
        )
        biometricPrompt.authenticate(promptInfo)
    }

    fun showToast(msg: String){
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show()
    }
}