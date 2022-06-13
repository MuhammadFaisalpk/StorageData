package com.example.storage_data.viewModel

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.example.storage_data.model.Documents
import com.example.storage_data.model.Images
import com.example.storage_data.model.Videos
import com.example.storage_data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: Repository

    init {
        repository = Repository(application)
    }

    private val images: MutableLiveData<List<Images>> by lazy {
        MutableLiveData<List<Images>>()
    }
    private val videos: MutableLiveData<List<Videos>> by lazy {
        MutableLiveData<List<Videos>>()
    }
    private val documents: MutableLiveData<List<Documents>> by lazy {
        MutableLiveData<List<Documents>>()
    }

    fun loadImages() {
        viewModelScope.launch() {
            doLoadImages()
        }
    }

    fun loadVideos() {
        viewModelScope.launch() {
            doLoadVideos()
        }
    }

    fun loadDocs() {
        viewModelScope.launch() {
            doLoadDocuments()
        }
    }

    private suspend fun doLoadImages() {
        withContext(Dispatchers.IO) {
            val allImages: ArrayList<Images> = repository.fetchAllImages()

            images.postValue(allImages)
        }
    }

    private suspend fun doLoadVideos() {
        withContext(Dispatchers.IO) {
            val allImages: ArrayList<Videos> = repository.fetchAllVideos()

            videos.postValue(allImages)
        }
    }

    private suspend fun doLoadDocuments() {
        withContext(Dispatchers.IO) {
            val allImages: ArrayList<Documents> = repository.fetchAllDocs()

            documents.postValue(allImages)
        }
    }

    fun getImages(): LiveData<List<Images>> = images
    fun getVideos(): LiveData<List<Videos>> = videos
    fun getDocs(): LiveData<List<Documents>> = documents
}