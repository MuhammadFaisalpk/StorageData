package com.example.storage_data.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.example.storage_data.model.MyModel
import com.example.storage_data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: Repository

    init {
        repository = Repository(application)
    }

    private val images: MutableLiveData<List<MyModel>> by lazy {
        MutableLiveData<List<MyModel>>()
    }
    private val videos: MutableLiveData<List<MyModel>> by lazy {
        MutableLiveData<List<MyModel>>()
    }
    private val documents: MutableLiveData<List<MyModel>> by lazy {
        MutableLiveData<List<MyModel>>()
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
            val allImages: ArrayList<MyModel> = repository.fetchAllImages()

            images.postValue(allImages)
        }
    }

    private suspend fun doLoadVideos() {
        withContext(Dispatchers.IO) {
            val allImages: ArrayList<MyModel> = repository.fetchAllVideos()

            videos.postValue(allImages)
        }
    }

    private suspend fun doLoadDocuments() {
        withContext(Dispatchers.IO) {
            val allImages: ArrayList<MyModel> = repository.fetchAllDocs()

            documents.postValue(allImages)
        }
    }

    fun getImages(): LiveData<List<MyModel>> = images
    fun getVideos(): LiveData<List<MyModel>> = videos
    fun getDocs(): LiveData<List<MyModel>> = documents
}