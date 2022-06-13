package com.example.storage_data.adapter

import android.app.Dialog
import android.app.RecoverableSecurityException
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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
import com.example.storage_data.R
import com.example.storage_data.model.Documents
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class DocsListAdapter(
    private val context: Fragment,
    private val resolutionForResult: ActivityResultLauncher<IntentSenderRequest>,
) :
    RecyclerView.Adapter<DocsListAdapter.ViewHolder>() {

    var items: ArrayList<Documents>? = null
    var newItem: Documents? = null
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
                                deleteFunction(it, document)
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

    fun getItemViewType(): Boolean {
        return isSwitchView
    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }

    fun onResult(requestCode: Int) {
        when (requestCode) {
            127 -> afterDeletePermission(newPosition)
            128 -> renameFunction(newPosition)
        }
    }

    private fun renameFunction(position: Int) {
        val dialog =
            Dialog(context.requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.rename_dialog_design)
        val name = dialog.findViewById(R.id.name) as EditText
        name.setText(items?.get(position)?.title)

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
                                newItem,
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
        newItem: Documents?, position: Int, newName: String, newFile: File
    ) {

        var newItem = Documents(
            newItem?.id,
            newName,
            newItem?.size,
            newFile.path,
            Uri.fromFile(newFile)
        )
        items?.removeAt(position)
        items?.add(position, newItem)
        notifyItemChanged(position)
    }

    private fun deleteFunction(view: View, docs: Documents?) {
        val file = docs?.path?.let { File(it) }
        val builder = MaterialAlertDialogBuilder(view.context)
        builder.setTitle("Delete Document?")
            .setMessage(docs?.title)
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
//        notifyItemChanged(position)
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