package com.example.storage_data.utils

import com.example.storage_data.model.MyModel


object MySingelton {

    private var arrayData: ArrayList<MyModel>? = ArrayList()
    private var selectedImages: ArrayList<MyModel>? = ArrayList()
    private var selectedVideos: ArrayList<MyModel>? = ArrayList()
    private var selectedDocs: ArrayList<MyModel>? = ArrayList()
    private var arrayPosition: Int = 0


    fun setData(data: ArrayList<MyModel>?) {
        arrayData = data
    }

    fun getData(): ArrayList<MyModel>? {
        return arrayData
    }

    fun setSelectedImages(data: MyModel) {
        if (selectedImages?.contains(data) == false) {
            selectedImages?.add(data)
        }
    }

    fun getSelectedImages(): ArrayList<MyModel>? {
        return selectedImages
    }

    fun removeSelectedImages(data: MyModel) {
        selectedImages?.remove(data)
    }

    fun setSelectedVideos(data: MyModel) {
        if (selectedVideos?.contains(data) == false) {
            selectedVideos?.add(data)
        }
    }

    fun getSelectedVideos(): ArrayList<MyModel>? {
        return selectedVideos
    }

    fun removeSelectedVideos(data: MyModel) {
        selectedVideos?.remove(data)
    }

    fun setSelectedDocs(data: MyModel) {
        if (selectedDocs?.contains(data) == false) {
            selectedDocs?.add(data)
        }
    }

    fun getSelectedDocs(): ArrayList<MyModel>? {
        return selectedDocs
    }

    fun removeSelectedDocs(data: MyModel) {
        selectedDocs?.remove(data)
    }

    fun setPosition(pos: Int) {
        arrayPosition = pos
    }

    fun getPosition(): Int {
        return arrayPosition
    }
}