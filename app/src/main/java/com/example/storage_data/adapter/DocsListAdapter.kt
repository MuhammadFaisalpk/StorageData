package com.example.storage_data.adapter

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.storage_data.R
import com.example.storage_data.model.Documents
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class DocsListAdapter(private val context: Fragment) :
    RecyclerView.Adapter<DocsListAdapter.ViewHolder>() {

    var items: ArrayList<Documents>? = null
    var newItem: Documents? = null
    private var newPosition = 0
    private val LIST_ITEM = 0
    private val GRID_ITEM = 1
    var isSwitchView = true

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = if (viewType == LIST_ITEM) {
            LayoutInflater.from(parent.context).inflate(R.layout.images_list_design, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.images_grid_design, parent, false)
        }
        return ViewHolder(itemView)
    }

    // binds the list items to a view
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val document = items?.get(position)

            holder.nameHolder.text = items?.get(position)?.title

            holder.optionHolder.setOnClickListener() {
                val popupMenu = PopupMenu(it.context, holder.optionHolder)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    newPosition = position

                    when (item.itemId) {
                        R.id.action_rename -> renameFunction(newPosition)
                        R.id.action_delete -> {
                            if (document != null) {
                                requestDeleteDocument(it, document)
                            }
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renameFunction(position: Int) {
        val dialog = Dialog(context.requireContext(), R.style.Theme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.rename_dialog_design)
        val name = dialog.findViewById(R.id.name) as EditText
        val ok = dialog.findViewById(R.id.ok) as Button
        ok.setOnClickListener {
            val newName = name.text.toString()
            if (newName.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val currentFile = items?.get(position)?.path?.let { File(it) }
                    if (currentFile != null) {
                        if (currentFile.exists() && newName
                                .isNotEmpty()
                        ) {
                            val newFile = File(
                                currentFile.parentFile,
                                newName + "." + currentFile.extension
                            )

                            val fromUri = Uri.withAppendedPath(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                                } else {
                                    MediaStore.Files.getContentUri("external")
                                },
                                items?.get(position)?.id
                            )

                            ContentValues().also {
                                it.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                                context.requireContext().contentResolver.update(
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
                                context.requireContext().contentResolver.update(
                                    fromUri,
                                    it,
                                    null,
                                    null
                                )
                            }

                            updateRenameUI(
                                newItem,
                                position,
                                newName = newName.toString(),
                                newFile = newFile
                            )
                        }
                    }
                    dialog.dismiss()
                } else {
                    val currentFile = items?.get(position)?.path?.let { it1 -> File(it1) }
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
                                    context.context,
                                    arrayOf(newFile.toString()),
                                    arrayOf("documents/*"),
                                    null
                                )
                            }
                            updateRenameUI(
                                newItem,
                                position = position,
                                newName = newName.toString(),
                                newFile = newFile
                            )
                        }

                    }
                    dialog.dismiss()
                }
            } else {
                name.error = "Field required."
            }
        }

        dialog.show()
    }

    private fun updateRenameUI(newItem: Documents?, position: Int, newName: String, newFile: File) {

        var newItem = Documents(
            newItem?.id,
            newName,
            newItem?.size,
            newFile.path,
            Uri.fromFile(newFile)
        )
        items?.removeAt(newPosition)
        items?.add(newPosition, newItem)
        notifyItemChanged(position)
    }

    private fun requestDeleteDocument(v: View?, docs: Documents?) {
        if (v != null) {
            val file = docs?.path?.let { File(it) }
            val builder = MaterialAlertDialogBuilder(v.context)
            builder.setTitle("Delete Document?")
                .setMessage(docs?.title)
                .setPositiveButton("Yes") { self, _ ->
                    if (file != null) {
                        if (file.exists() && file.delete()) {
                            MediaScannerConnection.scanFile(
                                v.context,
                                arrayOf(file.path),
                                null,
                                null
                            )
                            deleteFromList(newPosition)
                        }
                    }
                    self.dismiss()
                }
                .setNegativeButton("No") { self, _ -> self.dismiss() }
            val delDialog = builder.create()
            delDialog.show()
        }
    }

    private fun deleteFromList(position: Int) {
        items?.removeAt(position)
        notifyItemChanged(position)
    }

    fun onResult(requestCode: Int) {
        when (requestCode) {
            127 -> deleteFromList(newPosition)
            128 -> renameFunction(newPosition)
        }
    }

    override fun getItemCount(): Int {
        return if (items != null) {
            items!!.size
        } else {
            0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSwitchView) {
            LIST_ITEM
        } else {
            GRID_ITEM
        }
    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }

    fun setListItems(items: ArrayList<Documents>) {
        this.items = items
        notifyDataSetChanged()
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageHolder: ImageView = itemView.findViewById(R.id.imageView)
        val optionHolder: ImageView = itemView.findViewById(R.id.option)
        val nameHolder: TextView = itemView.findViewById(R.id.name)
        val fnameHolder: TextView = itemView.findViewById(R.id.foldername)
    }
}