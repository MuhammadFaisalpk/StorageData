package com.example.storage_data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.storage_data.model.Documents
import com.example.storage_data.model.Images
import com.example.storage_data.model.Videos
import com.example.storage_data.repository.Repository
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: Repository
    private lateinit var allImages: LiveData<ArrayList<Images>>
    private lateinit var allVideos: LiveData<ArrayList<Videos>>
    private lateinit var allDocs: LiveData<ArrayList<Documents>>

    init {
        repository = Repository(application)

        allImages = repository.allLocalImages
        allVideos = repository.allLocalVideos
        allDocs = repository.allLocalDocs
    }


    fun getAllImages(): LiveData<ArrayList<Images>> {
        return allImages
    }

    fun getAllVideos(): LiveData<ArrayList<Videos>> {
        return allVideos
    }

    fun getAllDocs(): LiveData<ArrayList<Documents>> {
        return allDocs
    }
}