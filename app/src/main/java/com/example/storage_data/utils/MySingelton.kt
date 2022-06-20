package com.example.storage_data.utils

import com.example.storage_data.model.MyModel


object MySingelton {

    private var arrayData: ArrayList<MyModel>? = ArrayList()
    private var selectedImages: ArrayList<MyModel>? = ArrayList()
    private var selectedVideos: ArrayList<MyModel>? = ArrayList()
    private var selectedDocs: ArrayList<MyModel>? = ArrayList()
    private var arrayPosition: Int = 0


    fun setImagesData(data: ArrayList<MyModel>?) {
        arrayData = data
    }

    fun getImagesData(): ArrayList<MyModel>? {
        return arrayData
    }

    fun setImagePosition(pos: Int) {
        arrayPosition = pos
    }

    fun getImagePosition(): Int {
        return arrayPosition
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
}