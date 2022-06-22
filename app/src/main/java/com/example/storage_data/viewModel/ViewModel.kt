package com.example.storage_data.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SavedModel
import com.example.storage_data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    private val saved: MutableLiveData<List<SavedModel>> by lazy {
        MutableLiveData<List<SavedModel>>()
    }

    fun getImages(): LiveData<List<MyModel>> = images
    fun getVideos(): LiveData<List<MyModel>> = videos
    fun getDocs(): LiveData<List<MyModel>> = documents
    fun getSaved(): LiveData<List<SavedModel>> = saved

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

    fun loadSaved() {
        viewModelScope.launch() {
            doLoadSaved()
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

    private suspend fun doLoadSaved() {
        withContext(Dispatchers.IO) {
            val allSaved: ArrayList<SavedModel> = repository.fetchAllSaved()

            saved.postValue(allSaved)
        }
    }
}