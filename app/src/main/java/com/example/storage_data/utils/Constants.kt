package com.example.storage_data.utils

import android.os.Environment
import com.example.storage_data.view.DocsFragment
import com.example.storage_data.view.ImagesFragment
import com.example.storage_data.view.SavedFragment
import com.example.storage_data.view.VideosFragment
import java.io.File

// shared preferences
const val FRAGMENT_SELECTION_PREFS = "fragment_selection_prefs"
const val LONG_PRESS_IMAGES = "long_press_images"
const val LONG_PRESS_VIDEOS = "long_press_videos"
const val LONG_PRESS_DOCS = "long_press_docs"

const val WRITE_STORAGE_PERMISSION_REQUEST_CODE = 13
const val MANAGE_APP_ALL_FILES_REQUEST_CODE = 120
const val DELETE_IMAGE_PERMISSION = 123
const val RENAME_IMAGE_PERMISSION = 124
const val DELETE_VIDEO_PERMISSION = 125
const val RENAME_VIDEO_PERMISSION = 126
const val DELETE_DOCS_PERMISSION = 127
const val RENAME_DOCS_PERMISSION = 128

val savedDirectoryName =
    File("${Environment.getExternalStorageDirectory()}/Download/StorageData/")

val fragmentsNames = arrayListOf(
    "Images", "Videos",
    "Docs", "Saved"
)
var fragmentsList = arrayOf(
    ImagesFragment(), VideosFragment(),
    DocsFragment(), SavedFragment()
)

