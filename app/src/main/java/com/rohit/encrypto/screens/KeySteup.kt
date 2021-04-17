package com.rohit.encrypto.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.rohit.encrypto.R
import com.rohit.encrypto.utils.SecurityHelper.Companion.generateAESKey

class KeySteup : AppCompatActivity() {
    private lateinit var mGeneratedKeyTextView: TextView
    lateinit var etOldKey: EditText
    lateinit var btnGeneratedKey: Button
    private lateinit var btnSaveOldKey: Button
    lateinit var btnCopyIt: ImageButton
    lateinit var btnKeySetupBack: ImageButton

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_steup)

        mGeneratedKeyTextView = findViewById(R.id.mGeneratedKeyTextView)
        etOldKey = findViewById(R.id.etOldKey)
        btnGeneratedKey = findViewById(R.id.btnGeneratedKey)
        btnSaveOldKey = findViewById(R.id.btnSaveOldKey)
        btnCopyIt = findViewById(R.id.btnCopyIt)
        btnKeySetupBack = findViewById(R.id.btnKeySetupBack)

        sharedPreference = getSharedPreferences("kjfowif93e9", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        var oldKey = sharedPreference.getString("OneTimeKey",null)
        if(!oldKey.isNullOrBlank()){
            etOldKey.setText(oldKey)
        }

        btnKeySetupBack.setOnClickListener { finish() }

        btnGeneratedKey.setOnClickListener {
            mGeneratedKeyTextView.text = generateAESKey()
        }

        btnCopyIt.setOnClickListener {
            copyIt()
        }

        mGeneratedKeyTextView.setOnClickListener {
            copyIt()
        }

        btnSaveOldKey.setOnClickListener {
            if (!etOldKey.text.isNullOrBlank()) {
                editor.putString("OneTimeKey", etOldKey.text.toString())
                editor.apply()
                showToast("Key saved & account activated successfully.")
                finish()
            }else{
                showToast("Please Enter One Time Account Key")
            }
        }
    }

    private fun copyIt() {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OneTimeKey", mGeneratedKeyTextView.text)
        clipboard.setPrimaryClip(clip)
        showToast("Copied")
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}