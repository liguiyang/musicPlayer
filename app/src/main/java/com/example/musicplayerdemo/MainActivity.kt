package com.example.musicplayerdemo

import android.content.pm.PackageManager
import android.media.MediaParser
import android.media.MediaPlayer
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    val musicPathList = mutableListOf<String>()
    val musicNameList = mutableListOf<String>()
    var current = 0
    var isPausing = false

    val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        } else {
            getMusicList()
        }
        mediaPlayer.setOnPreparedListener {
            it.start()
            seekBar.max = it.duration
        }
        mediaPlayer.setOnCompletionListener {
            next()
        }

        seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, position: Int, fromUser: Boolean) {
                if(fromUser){
                    mediaPlayer.seekTo(position)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        thread {
            while (true) {
                Thread.sleep(1000)
                runOnUiThread {
                    seekBar.progress = mediaPlayer.currentPosition
                }
            }
        }

    }


    fun onPlay(v: View){
     play()
    }

    fun onPause(v: View){
        if (isPausing){
            mediaPlayer.start()
            isPausing= true
        }else {
            mediaPlayer.pause()
            isPausing = false
        }
    }

    fun onStop(v: View){
        mediaPlayer.stop()
    }

    fun onNext(v: View){
        next()
    }

    private fun next() {
        current++
        if (current >= musicPathList.size) {
            current = 0
        }
        play()
    }

    fun onPrev(v: View){
        current--
        if (current < 0){
            current = musicPathList.size - 1
        }
        play()
    }

    fun play() {
        if (musicPathList.size == 0) return
        val musicPath = musicPathList.get(current)
        textView_count.text = "${current+1}/${musicPathList.size}"
        textView_musicName.text = musicNameList.get(current)
        mediaPlayer.reset()
        mediaPlayer.setDataSource(musicPath)
        mediaPlayer.prepareAsync()
    }


    fun getMusicList() {
        val cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null,null)
        cursor?.apply {
            while (moveToNext()){
                val musicPath = getString(getColumnIndex(MediaStore.Audio.Media.DATA))
                val musicName = getString(getColumnIndex(MediaStore.Audio.Media.TITLE))
                musicPathList.add(musicPath)
                musicNameList.add(musicName)
                Log.d("Music","$musicName: $musicPath")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getMusicList()
            }
        }
    }
}