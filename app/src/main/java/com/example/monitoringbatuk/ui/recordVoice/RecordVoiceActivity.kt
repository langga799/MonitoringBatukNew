package com.example.monitoringbatuk.ui.recordVoice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityRecordVoiceBinding
import com.example.monitoringbatuk.network.RetrofitBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.S)
class RecordVoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordVoiceBinding
    private val storageReference = Firebase.storage.reference
    private val firestore = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = Firebase.database.reference

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var timer: Timer? = null
    var recorder: MediaRecorder? = null


    var fileName = "default"
    var dirPath = ""

    private var nameUser = ""
    private var currentDir = ""
    private var fullDate = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnStopRecord.isEnabled = false


        binding.btnStartRecord.setOnClickListener {
            startRecordVoice()
        }

        binding.btnStopRecord.setOnClickListener {
            stopRecordVoive()
        }

        getNameUser()

    }


    @SuppressLint("SimpleDateFormat")
    private fun startRecordVoice() {
        if (!permissionIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 200)
            return
        }

        binding.apply {
            btnStartRecord.isEnabled = false
            btnStopRecord.isEnabled = true
        }


        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(dirPath + fileName)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(48000)

            try {
                prepare()
                start()
                Log.d("filnameeee", fileName)
            } catch (e: Exception) {
                Log.d("MediaRecorder", e.printStackTrace().toString())
            }
        }

        startDrawing()

    }


    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    val currentMaxAmplitude = recorder?.maxAmplitude
                    binding.recordVoice.update(currentMaxAmplitude ?: 0) //redraw view
                } catch (e: Exception) {
                    Log.d("StartDrawing", e.printStackTrace().toString())
                }

            }
        }, 0, 100)
    }


    private fun stopRecordVoive() {
        binding.apply {
            btnStartRecord.isEnabled = true
            btnStopRecord.isEnabled = false
        }

        recorder?.apply {
            stop()
            release()

        }
        currentDir = dirPath + fileName
        Log.d("Dir saat ini", currentDir)


        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = current.format(formatter)
        val formatTime = DateTimeFormatter.ofPattern("HH-mm-ss")
        val time = current.format(formatTime)
        fullDate = "${date}_${time}"

        saveFile()

        stopDrawing()
    }


    private fun saveFile() {
        val builder = android.app.AlertDialog.Builder(this)

        val view = LayoutInflater.from(this).inflate(R.layout.sheet_bottom, null, false)
        builder.setView(view)

        val edt = view.findViewById<TextInputEditText>(R.id.inputNew)
        edt.setText(currentDir)

        builder.setPositiveButton("Save") { _, _ ->

            val newFile = edt.text.toString()

            val src = File(dirPath + fileName)
            val dest = File("${newFile}_${fullDate}.wav")

            Log.d("src", src.toString())
            Log.d("dest", dest.toString())

            src.renameTo(dest)

            val path = Uri.fromFile(File(dest.toString()))
            Log.d("GET URI FILE", path.toString())

            sendAudioToStorage(path)


        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()

    }


    fun stopDrawing() {
        timer?.cancel()
        binding.recordVoice.recreate()
    }


    private fun permissionIsGranted(permission: Array<String>): Boolean {
        for (permiss in permission) {
            val check: Int = checkCallingOrSelfPermission(permiss)
            if (check != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result in grantResults) {
                return
            }
        }
    }


    private fun sendAudioToStorage(path: Uri) {

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress)
        val dialog: AlertDialog = builder.create()

        val metadata = storageMetadata {
            contentType = "audio/wav"
        }

        dialog.show()
        storageReference.child("audio/${path.lastPathSegment}")
            .putFile(path, metadata)
            .addOnCompleteListener { task ->
                Log.d("=====", task.result.task.snapshot.metadata?.reference.toString())

            }
            .addOnSuccessListener { data ->
                Log.d("UPLOAD_TASK", data.metadata.toString())
                val filename = data.metadata?.name
                val storageLocation = data.task.snapshot.metadata?.reference.toString()

                data.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    val url = it.toString()
                    Log.d("URL", it.toString())
                    if (filename != null) {
                        sendToHistoryAudio(filename, storageLocation, url, dialog)
                    }
                }



//                data.metadata?.reference?.downloadUrl?.addOnSuccessListener {
//                    Log.d("url", it.toString())
//                    url = it.toString()
//                }
                //  dialog.dismiss()


            }

    }


    private fun sendToHistoryAudio(filename: String, storageLocation: String, url:String, alertDialog: AlertDialog) {

        val dataHistoryAudio = hashMapOf(
            "date" to fullDate,
            "filename" to filename,
            "name" to nameUser,
            "location" to storageLocation,
            "url" to url,
        )
        firestore.collection("historyAudio")
            .add(dataHistoryAudio)
            .addOnSuccessListener {
                alertDialog.dismiss()
            }
    }

    private fun getNameUser() {
        val uid = firebaseAuth.uid
        val reference = databaseReference.child("UserData").child(uid.toString()).child("fullName")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    nameUser = snapshot.value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    // Send to Edge impulse
    private fun sendSample() {
        RetrofitBuilder.getApiService().uploadSample().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Response: ", response.toString())

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Error Response: ", t.message.toString())
            }

        })
    }


}