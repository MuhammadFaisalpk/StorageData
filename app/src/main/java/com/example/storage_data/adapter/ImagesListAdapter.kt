package com.example.storage_data.adapter

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.storage_data.R
import com.example.storage_data.model.Images
import com.example.storage_data.view.ImageSliderActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class ImagesListAdapter(
    private val context: Fragment,
) :
    RecyclerView.Adapter<ImagesListAdapter.ViewHolder>() {

    var items: ArrayList<Images>? = null
    private var newPosition = 0
    var newItem: Images? = null
    private val listItem = 0
    private val gridItem = 1
    var isSwitchView = true

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = if (viewType == listItem) {
            LayoutInflater.from(parent.context).inflate(R.layout.images_list_design, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.images_grid_design, parent, false)
        }
        return ViewHolder(itemView)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val images = items?.get(position)

            holder.nameHolder.text = items?.get(position)?.title
            holder.fnameHolder.text = items?.get(position)?.folderName

            val imagePath: String? = items?.get(position)?.path

            Glide.with(context)
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageHolder)

            holder.itemView.setOnClickListener() {
                val intent = Intent(it.context, ImageSliderActivity::class.java)
                intent.putExtra("image_position", position)
                intent.putExtra("images_list", items)
                it.context.startActivity(intent)
            }

            holder.optionHolder.setOnClickListener() {
                val popupMenu = PopupMenu(it.context, holder.optionHolder)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    newPosition = position
                    newItem = images

                    when (item.itemId) {
                        R.id.action_rename ->
                            renameFunction(newPosition)
                        R.id.action_delete ->
                            deleteFunction(it, images)
                    }
                    true
                }
                popupMenu.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            listItem
        } else {
            gridItem
        }
    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }

    fun getItemViewType(): Boolean {
        return isSwitchView
    }

    fun onResult(requestCode: Int) {
        when (requestCode) {
            125 -> afterDeletePermission(newPosition)
            126 -> afterRenamePermission(newPosition)
        }
    }

    private lateinit var newName: String

    private fun renameFunction(position: Int) {
        val dialog = Dialog(context.requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.rename_dialog_design)
        val name = dialog.findViewById(R.id.name) as EditText
        name.setText(items?.get(position)?.title)

        val ok = dialog.findViewById(R.id.ok) as Button
        ok.setOnClickListener {
            newName = name.text.toString()
            if (newName.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    dialog.dismiss()

                    val uriList: List<Uri> = listOf(
                        Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            items?.get(newPosition)?.id
                        )
                    )
                    val pi =
                        MediaStore.createWriteRequest(
                            context.requireContext().contentResolver,
                            uriList
                        )

                    (context.context as Activity).startIntentSenderForResult(
                        pi.intentSender, 126,
                        null, 0, 0, 0, null
                    )
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
                                    arrayOf("images/*"),
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

    private fun afterRenamePermission(position: Int) {
        //After dialog
        val currentFile = items?.get(position)?.path?.let { File(it) }
        if (currentFile != null) {
            if (currentFile.exists() && newName.toString()
                    .isNotEmpty()
            ) {
                val newFile = File(
                    currentFile.parentFile,
                    newName.toString() + "." + currentFile.extension
                )

                val fromUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
    }

    private fun updateRenameUI(
        newItem: Images?,
        position: Int,
        newName: String,
        newFile: File
    ) {
        var newItem = Images(
            newItem?.id,
            newName,
            newItem?.folderName,
            newItem?.size,
            newFile.path,
            Uri.fromFile(newFile)
        )
        items?.removeAt(position)
        items?.add(position, newItem)
        notifyItemChanged(position)
    }

    private fun deleteFunction(v: View?, images: Images?) {
        //list of images to delete
        val uriList: List<Uri> = listOf(
            Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                images?.id
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //requesting for delete permission
            val pi =
                MediaStore.createDeleteRequest(
                    context.requireContext().contentResolver,
                    uriList
                )
            if (v != null) {
                (v.context as Activity).startIntentSenderForResult(
                    pi.intentSender, 125,
                    null, 0, 0, 0, null
                )
            }
        } else {
            //for devices less than android 11
            if (v != null) {
                val file = images?.path?.let { File(it) }
                val builder = MaterialAlertDialogBuilder(v.context)
                builder.setTitle("Delete Image?")
                    .setMessage(images?.title)
                    .setPositiveButton("Yes") { self, _ ->
                        if (file != null) {
                            if (file.exists() && file.delete()) {
                                MediaScannerConnection.scanFile(
                                    v.context,
                                    arrayOf(file.path),
                                    null,
                                    null
                                )
                                afterDeletePermission(newPosition)
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

    private fun afterDeletePermission(position: Int) {
        items?.removeAt(position)
        notifyDataSetChanged()
//        notifyItemChanged(position )
    }

    fun setListItems(items: ArrayList<Images>) {
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