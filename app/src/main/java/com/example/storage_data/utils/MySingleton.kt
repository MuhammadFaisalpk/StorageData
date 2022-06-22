package com.example.storage_data.utils

import com.example.storage_data.model.MyModel


object MySingleton {

    private var imagesData: ArrayList<MyModel>? = ArrayList()
    private var videoData: MyModel? = null
    private var imagePosition: Int = 0


    fun setImagesData(data: ArrayList<MyModel>?) {
        imagesData = data
    }

    fun getImagesData(): ArrayList<MyModel>? {
        return imagesData
    }

    fun setImagePosition(pos: Int) {
        imagePosition = pos
    }

    fun getImagePosition(): Int {
        return imagePosition
    }

    fun setVideoData(data: MyModel?) {
        videoData = data
    }

    fun getVideoData(): MyModel? {
        return videoData
    }
}