package com.example.storage_data.utils

import android.view.View
import com.example.storage_data.model.Videos

interface DeleteInterface {
    fun requestDeleteR(v: View?, video: Videos)
    fun requestWriteR(v: View?, video: Videos, position: Int)
}