package com.rohit.encrypto.screens

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.rohit.encrypto.R
import com.rohit.encrypto.database.NoteDB
import com.rohit.encrypto.database.NoteEntity
import com.rohit.encrypto.recycler_view.CardAdapter
import com.rohit.encrypto.utils.EncAndDecUtil
import com.rohit.encrypto.utils.SearchState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {
    private lateinit var noteDB: NoteDB
    private var noteList: MutableList<NoteEntity> = ArrayList()

    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: CardAdapter
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var btnCreate: FloatingActionButton

    private lateinit var executor: Executor
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt

    private lateinit var txtTitle : TextView
    private lateinit var txtSearch : EditText
    private lateinit var btnSearch : ImageButton
    private lateinit var btnClear : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtTitle = findViewById(R.id.txtTitle)
        txtSearch = findViewById(R.id.txtSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnClear = findViewById(R.id.btnClear)

        noteDB = Room.databaseBuilder(this, NoteDB::class.java, "NoteDB").build()
        btnCreate = findViewById(R.id.btnCrete)
        recyclerView = findViewById(R.id.mRecyclerView)
        recyclerViewLayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = recyclerViewLayoutManager

        executor = ContextCompat.getMainExecutor(this)
        biometricManager = BiometricManager.from(this)

        authCheck()
        searchUIState(SearchState.CLEAR)

        btnCreate.setOnClickListener {
            var intent = Intent(this, CreateNote::class.java)
            intent.putExtra("action", "save")
            startActivity(intent)
        }

        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })

        btnSearch.setOnClickListener {
            searchUIState(SearchState.SEARCH)
        }

        btnClear.setOnClickListener {
            searchUIState(SearchState.CLEAR)
        }
    }

    override fun onResume() {
        super.onResume()
        setDataInRecyclerView()
    }

    private fun setDataInRecyclerView() {
        GlobalScope.launch {
            noteList = noteDB.noteDAO().getAllNotes()
            runOnUiThread {
                try {
                    adapter = CardAdapter(
                            this@MainActivity,
                            noteList
                    )
                    recyclerView!!.adapter = adapter
                    adapter.notifyDataSetChanged()

                    adapter.deleteTrustedUserClickListener = object : CardAdapter.DeleteClickListener {
                        override fun onBtnClick(noteEntity: NoteEntity) {
                            GlobalScope.launch {
                                noteDB.noteDAO().deleteNote(noteEntity)
                                runOnUiThread {
                                    setDataInRecyclerView()
                                }
                            }
                        }
                    }

                    adapter.editTrustedUserInfoClickListener = object : CardAdapter.EditClickListener{
                        override fun onBtnClick(noteEntity: NoteEntity) {
                            var intent = Intent(this@MainActivity, CreateNote::class.java)
                            intent.putExtra("action", "edit")
                            intent.putExtra("pk", noteEntity.pkId)
                            intent.putExtra("title", noteEntity.noteTitle)
                            intent.putExtra("description", noteEntity.noteDescription)
                            intent.putExtra("date", noteEntity.noteDate)
                            startActivity(intent)
                        }
                    }

                    adapter.unHideTrustedUserInfoClickListener = object : CardAdapter.UnHideClickListener{
                        override fun onBtnClick(
                                noteEntity: NoteEntity,
                                holder: CardAdapter.ViewHolder,
                                toggle: Boolean
                        ) {
                            val oa1 = ObjectAnimator.ofFloat(holder.cardView, "scaleX", 1f, 0f)
                            val oa2 = ObjectAnimator.ofFloat(holder.cardView, "scaleX", 0f, 1f)
                            oa1.interpolator = DecelerateInterpolator()
                            oa2.interpolator = AccelerateDecelerateInterpolator()
                            oa1.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    if (toggle) {
                                        val obj: EncAndDecUtil.SecuredData = Gson().fromJson(noteEntity.noteDescription, EncAndDecUtil.SecuredData::class.java)
                                        var decryptedDataStr = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                            EncAndDecUtil().decryptString(
                                                    obj.value,
                                                    obj.encryptedValue
                                            )
                                        } else {
                                            null
                                        }
                                        holder.description.text = decryptedDataStr
                                        holder.description.visibility = View.VISIBLE
                                        //cardView.setImageResource(R.drawable.frontSide)
                                    } else {
                                        holder.description.visibility = View.GONE
                                    }
                                    oa2.start()
                                }
                            })
                            oa1.start()
                        }

                    }
                } catch (e: Exception) {
                    println("================== noteList 4: $e")
                }
            }
        }
    }

    private fun searchUIState(state: SearchState){
        when(state){
            SearchState.CLEAR -> {
                txtTitle.visibility = View.VISIBLE
                btnSearch.visibility = View.VISIBLE
                txtSearch.visibility = View.GONE
                btnClear.visibility = View.GONE
            }
            SearchState.SEARCH -> {
                txtTitle.visibility = View.GONE
                btnSearch.visibility = View.GONE
                txtSearch.visibility = View.VISIBLE
                btnClear.visibility = View.VISIBLE
            }
        }
    }

    fun filter(text: String) {
        println("===================== text1111:  $text")
        val temp: MutableList<NoteEntity> = ArrayList()
        for (obj in noteList) {
            if (obj.noteTitle!!.contains(text, true)) {
                println("===================== text2222:  $text")
                temp.add(obj)
            }
        }
        adapter.updateDataInRecyclerView(temp)
    }


    /** For fingerprint auth*/
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
                //.setSubtitle("Authentication Required!")
                .setDescription("This app uses biometric authentication to protect your data.")
                .setDeviceCredentialAllowed(true)
                .setConfirmationRequired(true)
                .build()

        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        biometricPrompt.cancelAuthentication()
                        //showToast("Authentication Pass")
                        //startActivity(Intent(this@Auth, MainActivity::class.java))
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        finishAffinity()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        showToast("Authentication Fail")
                    }
                }
        )
        biometricPrompt.authenticate(promptInfo)
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}