package com.example.storage_data.activities

import android.os.Bundle
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.storage_data.R
import com.example.storage_data.databinding.ActivityVideoPlayerBinding
import com.example.storage_data.model.MyModel
import com.example.storage_data.utils.MySingleton

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    lateinit var videoView: VideoView
    lateinit var titleView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        getSetData()
    }

    private fun initViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)

        videoView = binding.videoView
        titleView = binding.title
    }

    private fun getSetData() {
        val videoData = MySingleton.getVideoData()
        titleView.text = videoData?.title

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        mediaController.requestFocus()
        videoView.setMediaController(mediaController)
        videoView.setVideoPath(videoData?.path)
        videoView.requestFocus()
        videoView.start()
    }

}