package com.example.monitoringbatuk.ui.historyAudio

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityHistoryAudioBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryAudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryAudioBinding
    private val firestore = Firebase.firestore
    private val adapter = HistoryAudioAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.apply {
            title = "History Audio"
            setTitleTextColor(ContextCompat.getColor(baseContext, R.color.white))
            navigationIcon =
                ContextCompat.getDrawable(baseContext, R.drawable.ic_round_arrow_back_24)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }



        binding.progressBar2.visibility = View.VISIBLE
        firestore.collection("historyAudio")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                binding.progressBar2.visibility = View.INVISIBLE
                for (doc in task.result) {
                    val filename = doc.get("filename") as String
                    val name = doc.get("name") as String
                    val dateAndTime = doc.get("date") as String
                    val location = doc.get("location") as String
                    val url = doc.get("url") as String

                    adapter.addAllData(
                        arrayListOf(
                            AudioHistory(
                                dateAndTime,
                                filename,
                                name,
                                location,
                                url
                            )
                        )
                    )
                }


            }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvAudioHistory.adapter = adapter
            rvAudioHistory.layoutManager = LinearLayoutManager(this@HistoryAudioActivity)
            rvAudioHistory.setHasFixedSize(true)


        }
    }
}