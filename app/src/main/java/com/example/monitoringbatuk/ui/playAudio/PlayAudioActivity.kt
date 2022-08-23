

package com.example.monitoringbatuk.ui.playAudio

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.jean.jcplayer.model.JcAudio
import com.example.monitoringbatuk.databinding.ActivityPlayAudioBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Suppress("DEPRECATION")
class PlayAudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayAudioBinding

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    private var pause: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("URL_AUDIO").toString()


        // Start Playing
        mediaPlayer = MediaPlayer()
        binding.btnPlayAudio.setOnClickListener {
            if (pause){
                mediaPlayer.setDataSource(url)
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
                mediaPlayer.start()
                pause = false

            } else {
                mediaPlayer.setDataSource(url)
                mediaPlayer.start()

            }
        }

        initialSeekBar()
        binding.btnPlayAudio.isEnabled = false
        binding.btnPauseAudio.isEnabled = true

        mediaPlayer.setOnCompletionListener {
            binding.btnPlayAudio.isEnabled = true
            binding.btnPauseAudio.isEnabled = false
        }


        // Pause media player
        binding.btnPauseAudio.setOnClickListener {
            if (mediaPlayer.isPlaying){
                mediaPlayer.pause()
                pause = true
                binding.btnPlayAudio.isEnabled = true
                binding.btnPauseAudio.isEnabled = false
            }
        }













        binding.btnPlayAudio.setOnClickListener {

            val mediaPlayer = MediaPlayer()
            try {
                 Log.d("URL_AUDIO", url)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.setDataSource(url)
                mediaPlayer.setOnPreparedListener { mp ->
                    mp?.start()
                }
                mediaPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        val player = ArrayList<JcAudio>()
        player.add(JcAudio.createFromURL(url))
        binding.player.initPlaylist(player, null)


        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    mediaPlayer.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


    }

    private fun initialSeekBar() {
        binding.seekBar.max = mediaPlayer.duration.seconds.toInt(DurationUnit.SECONDS)

        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer.currentPosition.seconds.toInt(DurationUnit.SECONDS)
            binding.tvStart.text = mediaPlayer.currentPosition.seconds.toString()
            val diff = mediaPlayer.duration.seconds.toInt(DurationUnit.SECONDS) - mediaPlayer.currentPosition.seconds.toInt(DurationUnit.SECONDS)
            binding.tvEnd.text = diff.toString()
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }
}