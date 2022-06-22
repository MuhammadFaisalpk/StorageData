package com.example.storage_data.adapter

import android.app.Dialog
import android.media.MediaScannerConnection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.storage_data.R
import com.example.storage_data.model.SavedModel
import com.example.storage_data.model.SelectedModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class SavedListAdapter(
    private val context: Fragment,
) :
    RecyclerView.Adapter<SavedListAdapter.ViewHolder>() {

    var items: ArrayList<SavedModel>? = null
    var newItem: SavedModel? = null
    private var newPosition = 0
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
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        try {
            val position = holder.adapterPosition

            val files = items?.get(position)
            var name = files?.name
            var path = files?.path

            holder.nameHolder.text = name

            Glide.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageHolder)

            holder.optionHolder.setOnClickListener() {
                val popupMenu = PopupMenu(it.context, holder.optionHolder)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    newPosition = position
                    newItem = files

                    when (item.itemId) {
                        R.id.action_rename -> renameFunction(newPosition)
                        R.id.action_delete -> {
                            if (files != null) {
                                deleteFunction(it, files)
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
        val dialog =
            Dialog(context.requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.rename_dialog_design)
        val name = dialog.findViewById(R.id.name) as EditText
        name.setText(items?.get(position)?.name)

        val ok = dialog.findViewById(R.id.ok) as Button
        ok.setOnClickListener {
            val newName = name.text.toString()

            if (newName.isNotEmpty()) {
                try {
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
                                position = position,
                                newName = newName.toString(),
                                newFile = newFile
                            )
                        }

                    }

                } catch (e: Exception) {
                    Log.d("AdapterException", "" + e.toString())
                }
                dialog.dismiss()
            } else {
                name.error = "Field required."
            }
        }

        dialog.show()
    }

    private fun updateRenameUI(
        position: Int, newName: String, newFile: File
    ) {

        val newItem = SavedModel(
            newName,
            newFile.path,
        )
        items?.removeAt(position)
        items?.add(position, newItem)
        notifyItemChanged(position)
    }

    private fun deleteFunction(view: View, docs: SavedModel?) {
        val file = docs?.path?.let { File(it) }
        val builder = MaterialAlertDialogBuilder(view.context)
        builder.setTitle("Delete file?")
            .setMessage(docs?.name)
            .setPositiveButton("Yes") { self, _ ->
                try {
                    if (file != null) {
                        if (file.exists() && file.delete()) {
                            MediaScannerConnection.scanFile(
                                view.context,
                                arrayOf(file.path),
                                null,
                                null
                            )
                            afterDeletePermission(newPosition)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("AdapterException", "" + e.toString())
                }
                self.dismiss()
            }
            .setNegativeButton("No") { self, _ -> self.dismiss() }
        val delDialog = builder.create()
        delDialog.show()
    }

    private fun afterDeletePermission(position: Int) {
        items?.removeAt(position)
        notifyDataSetChanged()
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

    fun setListItems(data: ArrayList<SavedModel>) {
        items = data
        notifyDataSetChanged()
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageHolder: ImageView = itemView.findViewById(R.id.imageView)
        val optionHolder: ImageView = itemView.findViewById(R.id.option)
        val nameHolder: TextView = itemView.findViewById(R.id.name)
        val fnameHolder: TextView = itemView.findViewById(R.id.foldername)
        val clMain: ConstraintLayout = itemView.findViewById(R.id.cl_main)
    }

}