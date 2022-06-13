package com.example.storage_data.repository

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.storage_data.model.Documents
import com.example.storage_data.model.Images
import com.example.storage_data.model.Videos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class Repository(private val application: Application) {

    fun fetchAllImages(): ArrayList<Images> {
        val listImages: ArrayList<Images> = ArrayList()

        val columns = arrayOf(
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_ID
        )

        val orderBy = MediaStore.Images.Media.DATE_TAKEN //order data by date
        val collection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = application.contentResolver.query(
            collection, columns, null,
            null, "$orderBy DESC"
        ) //get all data in Cursor by sorting in DESC order

        if (cursor != null) {
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                val ID: Int =
                    cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val DATA: Int =
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val TITLE: Int =
                    cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                val BUCKET_DISPLAY_NAME: Int =
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val BUCKET_ID: Int =
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                val SIZE: Int =
                    cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

                val id: String = cursor.getString(ID)
                val path: String = cursor.getString(DATA)
                val title: String = cursor.getString(TITLE)
                val folderName: String? = cursor.getString(BUCKET_DISPLAY_NAME)
                val folderID: String = cursor.getString(BUCKET_ID)
                val size: String = cursor.getString(SIZE)

                val file = File(path)
                val artUriC = Uri.fromFile(file)

                listImages.add(
                    Images(
                        id,
                        title,
                        folderName,
                        size,
                        path,
                        artUriC
                    )
                ) //get Image from column index
            }
            cursor.close()
        }
        return listImages
    }

    fun fetchAllVideos(): ArrayList<Videos> {
        val listVideos: ArrayList<Videos> = ArrayList()

        val columns = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_ID
        )
        val orderBy = MediaStore.Video.Media.DATE_TAKEN //order data by date
        val collection: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = application.contentResolver.query(
            collection, columns, null,
            null, "$orderBy DESC"
        ) //get all data in Cursor by sorting in DESC order

        if (cursor != null) {
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                val ID: Int =
                    cursor.getColumnIndex(MediaStore.Video.Media._ID)
                val DATA: Int =
                    cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val TITLE: Int =
                    cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val BUCKET_DISPLAY_NAME: Int =
                    cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val BUCKET_ID: Int =
                    cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                val SIZE: Int =
                    cursor.getColumnIndex(MediaStore.Video.Media.SIZE)

                val id: String = cursor.getString(ID)
                val path: String = cursor.getString(DATA)
                val title: String = cursor.getString(TITLE)
                val folderName: String? = cursor.getString(BUCKET_DISPLAY_NAME)
                val folderID: String = cursor.getString(BUCKET_ID)
                val size: String = cursor.getString(SIZE)

                val file = File(path)
                val artUriC = Uri.fromFile(file)

                listVideos.add(
                    Videos(
                        id,
                        title,
                        folderName,
                        size,
                        path,
                        artUriC
                    )
                ) //get Image from column index
            }
            cursor.close()
        }
        return listVideos
    }

    fun fetchAllDocs(): ArrayList<Documents> {
        val listDocuments: ArrayList<Documents> = ArrayList()
        val mutableListDocuments = MutableLiveData<ArrayList<Documents>>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        application.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        ).use { cursor ->
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val columnID: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                    val columnData: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnSize: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                    do {
                        val id: String = cursor.getString(columnID)
                        val path: String = cursor.getString(columnData)
                        val name: String = cursor.getString(columnName)
                        val size: String = cursor.getString(columnSize)

                        val file = File(path)
                        val fileUri = Uri.fromFile(file)
                        if (file.exists()) {
                            //you can get your pdf files
                            listDocuments.add(
                                Documents(
                                    id,
                                    name,
                                    size,
                                    path,
                                    fileUri
                                )
                            )
                        }
                    } while (cursor.moveToNext())
                }
            }
        }
        return listDocuments
    }

}