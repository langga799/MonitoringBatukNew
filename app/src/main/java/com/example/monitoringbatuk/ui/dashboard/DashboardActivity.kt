package com.example.monitoringbatuk.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.monitoringbatuk.databinding.ActivityDashboardBinding
import com.example.monitoringbatuk.helper.PreferenceDataStore
import com.example.monitoringbatuk.helper.PreferenceViewModel
import com.example.monitoringbatuk.helper.ViewModelFactory
import com.example.monitoringbatuk.ui.history.SearchHistoryActivity
import com.example.monitoringbatuk.ui.historyAudio.HistoryAudioActivity
import com.example.monitoringbatuk.ui.login.LoginActivity
import com.example.monitoringbatuk.ui.record.RecordActivity
import com.example.monitoringbatuk.ui.recordVoice.RecordVoiceActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var preferenceViewModel: PreferenceViewModel
    private val Context.dataStorePref by preferencesDataStore(name = "settings")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val pref = PreferenceDataStore.getInstance(this.dataStorePref)
        preferenceViewModel = ViewModelProvider(this,
            ViewModelFactory(pref))[PreferenceViewModel::class.java]



        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference



        navigationController()
        getDataUserInfo()


        binding.btnLogout.setOnClickListener {
            showAlert()
        }

    }


    private fun showAlert() { // fungsi untuk menampilkan dialog logout
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout Account")
            .setMessage("Do you want to logout of the account?")
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(baseContext, "Action was canceled", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Yes") { _, _ ->
                logoutAccount()
            }
            .show()
    }


    private fun logoutAccount() { // fungsi untuk keluar dari akun
        firebaseAuth.signOut()
        preferenceViewModel.saveLogin(false)

        Toast.makeText(this, "Logout success", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()

    }


    private fun getDataUserInfo() { // untuk mendapatkan data nama user yang akan ditampilkan di textview
        val dataName = firebaseAuth.uid
        databaseReference.child("UserData").child("$dataName").child("fullName")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.tvPersonName.text = snapshot.value.toString()

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(baseContext, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigationController() { // fungsi untuk mengatur navigasi antar activity
        binding.apply {
            btnDataRecord.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, RecordActivity::class.java))
            }

            btnToHistory.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, SearchHistoryActivity::class.java))
            }

            btnToVoiceRecord.setOnClickListener {
                startActivity(Intent(this@DashboardActivity,RecordVoiceActivity::class.java))
            }

            btnToHistoryAudio.setOnClickListener {
                startActivity(Intent(this@DashboardActivity,HistoryAudioActivity::class.java))
            }
        }

    }
}