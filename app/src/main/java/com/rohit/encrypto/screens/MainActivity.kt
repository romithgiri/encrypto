package com.rohit.encrypto.screens

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rohit.encrypto.R
import com.rohit.encrypto.database.NoteDB
import com.rohit.encrypto.database.NoteEntity
import com.rohit.encrypto.recycler_view.CardAdapter
import com.rohit.encrypto.utils.SearchState
import com.rohit.encrypto.utils.SecurityHelper
import com.rohit.encrypto.utils.StoragePermissionCheck
import ir.androidexception.roomdatabasebackupandrestore.Backup
import ir.androidexception.roomdatabasebackupandrestore.Restore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
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

    private lateinit var txtNoData: TextView
    private lateinit var txtTitle: TextView
    private lateinit var txtSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var btnClear: ImageButton

    private var fabExpanded = false
    private var fabMenu: FloatingActionButton? = null
    private var fabCreate: FloatingActionButton? = null
    private var fabRestore: FloatingActionButton? = null
    private var fabBackup: FloatingActionButton? = null
    private var fabKeySetup: FloatingActionButton? = null
    private var layoutFabCreateNote: LinearLayout? = null
    private var layoutFabRestore: LinearLayout? = null
    private var layoutFabBackup: LinearLayout? = null
    private var layoutFabKeySetup: LinearLayout? = null

    // Storage Permissions
    private val REQUEST_PERMISSION = 1
    private val PICKFILE_RESULT_CODE = 2
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var databaseTask: Boolean = false
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //id init
        txtNoData = findViewById(R.id.txtNoData)
        txtTitle = findViewById(R.id.txtTitle)
        txtSearch = findViewById(R.id.txtSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnClear = findViewById(R.id.btnClear)
        //for floating button
        fabMenu = findViewById<View>(R.id.fabMenu) as FloatingActionButton
        fabCreate = findViewById<View>(R.id.fabCreateNote) as FloatingActionButton
        fabBackup = findViewById<View>(R.id.fabBackup) as FloatingActionButton
        fabRestore = findViewById<View>(R.id.fabRestore) as FloatingActionButton
        fabKeySetup = findViewById<View>(R.id.fabKeySetup) as FloatingActionButton
        layoutFabCreateNote = findViewById<View>(R.id.layoutFabCreateNote) as LinearLayout
        layoutFabRestore = findViewById<View>(R.id.layoutFabRestore) as LinearLayout
        layoutFabBackup = findViewById<View>(R.id.layoutFabBackup) as LinearLayout
        layoutFabKeySetup = findViewById<View>(R.id.layoutFabKeySetup) as LinearLayout
        //room DB
        noteDB = Room.databaseBuilder(this, NoteDB::class.java, "NoteDB").build()
        btnCreate = findViewById(R.id.btnCrete)
        recyclerView = findViewById(R.id.mRecyclerView)
        recyclerViewLayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = recyclerViewLayoutManager
        //for auth
        executor = ContextCompat.getMainExecutor(this)
        biometricManager = BiometricManager.from(this)
        //other init method
        authCheck()
        searchUIState(SearchState.CLEAR)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        );

        fabCreate!!.setOnClickListener {
            var intent = Intent(this, CreateNote::class.java)
            intent.putExtra("action", "save")
            startActivity(intent)
        }

        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
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

        fabMenu!!.setOnClickListener {
            if (fabExpanded) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }

        fabBackup!!.setOnClickListener {
            createBackup()
        }

        fabRestore!!.setOnClickListener {
            restoreBackup()
        }

        fabKeySetup!!.setOnClickListener {
            startActivity(Intent(this, KeySteup::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreference = getSharedPreferences(
            "kjfowif93e9",
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        setDataInRecyclerView()
        closeSubMenusFab()
    }

    //closes FAB submenus
    private fun closeSubMenusFab() {
        layoutFabCreateNote!!.visibility = View.INVISIBLE
        layoutFabRestore!!.visibility = View.INVISIBLE
        layoutFabBackup!!.visibility = View.INVISIBLE
        layoutFabKeySetup!!.visibility = View.INVISIBLE
        fabMenu!!.setImageResource(R.drawable.menu)
        fabExpanded = false
    }

    //Opens FAB submenus
    private fun openSubMenusFab() {
        layoutFabCreateNote!!.visibility = View.VISIBLE
        layoutFabRestore!!.visibility = View.VISIBLE
        layoutFabBackup!!.visibility = View.VISIBLE
        layoutFabKeySetup!!.visibility = View.VISIBLE
        fabMenu!!.setImageResource(R.drawable.clear)
        fabExpanded = true
    }

    private fun setDataInRecyclerView() {
        GlobalScope.launch {
            noteList = noteDB.noteDAO().getAllNotes()
            noteList.sortBy { noteEntity: NoteEntity -> noteEntity.noteTitle }
            runOnUiThread {
                try {
                    adapter = CardAdapter(
                        this@MainActivity,
                        noteList
                    )
                    recyclerView!!.adapter = adapter
                    adapter.notifyDataSetChanged()

                    if (noteList.isNullOrEmpty()) {
                        txtNoData.visibility = View.VISIBLE
                        recyclerView!!.visibility = View.GONE
                    } else {
                        txtNoData.visibility = View.GONE
                        recyclerView!!.visibility = View.VISIBLE
                    }

                    adapter.deleteTrustedUserClickListener =
                        object : CardAdapter.DeleteClickListener {
                            override fun onBtnClick(noteEntity: NoteEntity) {
                                deleteConfirmation(noteEntity)
                            }
                        }

                    adapter.editTrustedUserInfoClickListener =
                        object : CardAdapter.EditClickListener {
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

                    adapter.unHideTrustedUserInfoClickListener =
                        object : CardAdapter.UnHideClickListener {
                            override fun onBtnClick(
                                noteEntity: NoteEntity,
                                holder: CardAdapter.ViewHolder,
                                toggle: Boolean
                            ) {
                                var oldKey = sharedPreference.getString("OneTimeKey", null)
                                if (oldKey.isNullOrBlank()) {
                                    showToast("Please setup your one time account key first.")
                                } else {
                                    val oa1 =
                                        ObjectAnimator.ofFloat(holder.cardView, "scaleX", 1f, 0f)
                                    val oa2 =
                                        ObjectAnimator.ofFloat(holder.cardView, "scaleX", 0f, 1f)
                                    oa1.interpolator = DecelerateInterpolator()
                                    oa2.interpolator = AccelerateDecelerateInterpolator()
                                    oa1.addListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            super.onAnimationEnd(animation)
                                            if (toggle) {
                                                holder.description.text =
                                                    SecurityHelper().decryptAES(
                                                        noteEntity.noteDescription,
                                                        oldKey
                                                    )
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
                        }
                } catch (e: Exception) {
                    Log.e("TAG", "Error: $e")
                }
            }
        }
    }

    private fun searchUIState(state: SearchState) {
        when (state) {
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
        val temp: MutableList<NoteEntity> = ArrayList()
        for (obj in noteList) {
            if (obj.noteTitle!!.contains(text, true)) {
                temp.add(obj)
            }
        }
        adapter.updateDataInRecyclerView(temp)
    }

    private fun createBackup() {
        databaseTask = false
        if (!sharedPreference.getString("OneTimeKey", null).isNullOrBlank()) {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                if (StoragePermissionCheck.checkPermission(this)) {
                    executeBackupTask()
                } else {
                    requestForStoragePermission()
                }
            }
        }else{
            showToast("Please setup your one time account key first.")
        }
    }

    private fun restoreBackup() {
        databaseTask = true
        if (!sharedPreference.getString("OneTimeKey", null).isNullOrBlank()) {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                if (StoragePermissionCheck.checkPermission(this)) {
                    val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                    chooseFile.type = "text/plain"
                    startActivityForResult(
                        Intent.createChooser(chooseFile, "Choose a file"),
                        PICKFILE_RESULT_CODE
                    )
                } else {
                    requestForStoragePermission()
                }
            }
        } else {
            showToast("Please setup your one time account key first.")
        }
    }

    private fun executeRestoreTask(filePath: String) {
        Restore.Init()
            .database(noteDB)
            .backupFilePath(filePath)
            .secretKey("080910") // if your backup file is encrypted, this parameter is required
            .onWorkFinishListener { success, message ->
                if (success) {
                    showToast("Database restored successfully")
                    setDataInRecyclerView()
                } else {
                    showToast("Error: Backup restoration fail")
                }
            }
            .execute()
    }

    private fun executeBackupTask() {
        try {
            var backupPath = File(application.externalCacheDir, "backup")
            val finalPathOfBackupFileSubstring: String =
                backupPath.path.substring(0, backupPath.path.indexOf("/Android"))
            val filePath =
                finalPathOfBackupFileSubstring + File.separator + "Documents" + File.separator + "backup"
            if (!File(filePath).exists()) {
                File(filePath).mkdir()
            }
            val fileName = "encrypto_backup" + System.currentTimeMillis().toString() + ".txt"
            val finalPathOfBackupFile = filePath + File.separator + fileName
            Backup.Init()
                .database(noteDB)
                .path(filePath)
                .fileName(fileName)
                .secretKey("080910") //optional
                .onWorkFinishListener { success, message ->
                    showToast("Backup created successfully at path: $finalPathOfBackupFile")
                }
                .execute()
        } catch (e: java.lang.Exception) {
            Log.e("TAG", "Error: $e")
        }
    }

    //The below method can be used for requesting a permission in android 11 or below
    private fun requestForStoragePermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    String.format(
                        "package:%s",
                        applicationContext.packageName
                    )
                )
                startActivityForResult(intent, 102)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 102)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST_PERMISSION
            )
        }
    }

    //Handling permission callback for Android 11 or above versions
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                    if (databaseTask) {
                        restoreBackup()
                    } else {
                        executeBackupTask()
                    }
                } else {
                    showToast("Allow permission for storage access!")
                }
            }
        } else if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            try {
                if (data != null) {
                    val selectedFile = data.data //The uri with the location of the file
                    if (selectedFile != null) {
                        executeRestoreTask(selectedFile.path!!)
                    } else {
                        showToast("Error: no file selected")
                    }
                } else {
                    showToast("Error: no file selected")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error: $e")
                showToast("Error: no file selected")
            }
        }
    }

    //Handling permission callback for OS versions below Android 11
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION -> if (grantResults.isNotEmpty()) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    // perform action when allow permission success
                    if (databaseTask) {
                        restoreBackup()
                    } else {
                        executeBackupTask()
                    }
                } else {
                    showToast("Allow permission for storage access!")
                }
            }
        }
    }


    fun deleteConfirmation(noteEntity: NoteEntity) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete")
        builder.setMessage("Do your want to Delete?")
        builder.setPositiveButton("Delete") { dialog, which ->
            GlobalScope.launch {
                noteDB.noteDAO().deleteNote(noteEntity)
                runOnUiThread {
                    setDataInRecyclerView()
                    dialog.dismiss()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    /*private fun deleteConfirmation(noteEntity: NoteEntity): AlertDialog {
        return AlertDialog.Builder(this) // set message, title, and icon
            .setTitle("Delete")
            .setMessage("Do you want to Delete")
            .setIcon(R.drawable.delete)
            .setPositiveButton("Delete") { dialog, whichButton ->
                //your deleting code
                GlobalScope.launch {
                    noteDB.noteDAO().deleteNote(noteEntity)
                    runOnUiThread {
                        setDataInRecyclerView()
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton("cancel") { dialog, which -> dialog.dismiss() }
            .create()
    }*/

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