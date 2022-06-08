package com.example.storage_data.utils

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.storage_data.R
import com.example.storage_data.model.Videos
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class Helper(val context: Context) : DeleteInterface {

    fun onIntent(i: Intent?, resultCode: Int, requestCode: Int) {
        when (requestCode) {
            123 -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show()
            }
            124 -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "Rename.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun requestDeleteR(v: View?, item: Videos) {
        //list of videos to delete
        val uriList: List<Uri> = listOf(
            Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                item.id
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //requesting for delete permission
            if (v != null) {
                val pi =
                    MediaStore.createDeleteRequest(v.context.contentResolver, uriList)

                (v.context as Activity).startIntentSenderForResult(
                    pi.intentSender, 123,
                    null, 0, 0, 0, null
                )
            }
        } else {
            //for devices less than android 11
            if (v != null) {
                val file = item.path?.let { File(it) }
                val builder = MaterialAlertDialogBuilder(v.context)
                builder.setTitle("Delete Video?")
                    .setMessage(item.title)
                    .setPositiveButton("Yes") { self, _ ->
                        if (file != null) {
                            if (file.exists() && file.delete()) {
                                MediaScannerConnection.scanFile(
                                    v.context,
                                    arrayOf(file.path),
                                    null,
                                    null
                                )
                            }

                        }
                        self.dismiss()
                    }
                    .setNegativeButton("No") { self, _ -> self.dismiss() }
                val delDialog = builder.create()
                delDialog.show()
            }
        }
    }

    override fun requestWriteR(v: View?, item: Videos, position: Int) {
        //files to modify
        val uriList: List<Uri> = listOf(
            Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                item.id
            )
        )
        //requesting file write permission for specific files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi =
                v?.context?.let { MediaStore.createWriteRequest(it.contentResolver, uriList) }
            if (pi != null) {
                (v.context as Activity).startIntentSenderForResult(
                    pi.intentSender, 124,
                    null, 0, 0, 0, null
                )
            }
        } else renameFunction(v, item)
    }

    private fun renameFunction(v: View?, item: Videos) {
        val dialog = v?.context?.let { Dialog(v.context) }
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setCancelable(true)

            dialog.setContentView(R.layout.rename_dialog_design)
            val name = dialog.findViewById(R.id.name) as EditText
            val ok = dialog.findViewById(R.id.ok) as Button
            ok.setOnClickListener {
                val newName = name.text.toString()
                if (newName.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val currentFile = item.path?.let { File(it) }
                        if (currentFile != null) {
                            if (currentFile.exists() && newName.toString()
                                    .isNotEmpty()
                            ) {
                                val newFile = File(
                                    currentFile.parentFile,
                                    newName.toString() + "." + currentFile.extension
                                )

                                val fromUri = Uri.withAppendedPath(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    item.id
                                )

                                ContentValues().also {
                                    it.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                                    v.context.contentResolver.update(
                                        fromUri,
                                        it,
                                        null,
                                        null
                                    )
                                    it.clear()

                                    //updating file details
                                    it.put(
                                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                                        newName.toString()
                                    )
                                    it.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                                    v.context.contentResolver.update(
                                        fromUri,
                                        it,
                                        null,
                                        null
                                    )
                                }

//                            updateRenameUI(
//                                position,
//                                newName = newName.toString(),
//                                newFile = newFile
//                            )
                            }
                        }
                        dialog.dismiss()
                    } else {
                        val currentFile = item.path?.let { it1 -> File(it1) }
                        if (currentFile != null) {
                            if (currentFile.exists() && newName.toString()
                                    .isNotEmpty()
                            ) {
                                val newFile = File(
                                    currentFile.parentFile,
                                    newName.toString() + "." + currentFile.extension
                                )
                                if (currentFile.renameTo(newFile)) {
                                    MediaScannerConnection.scanFile(
                                        v.context,
                                        arrayOf(newFile.toString()),
                                        arrayOf("video/*"),
                                        null
                                    )
                                    //                            updateRenameUI(
                                    //                                position = position,
                                    //                                newName = newName.toString(),
                                    //                                newFile = newFile
                                    //                            )
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                } else {
                    name.error = "Field required."
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }

}

