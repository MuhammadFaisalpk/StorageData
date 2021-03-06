package com.example.storage_data.adapter

import android.app.Dialog
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.system.Os.rename
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.interfaces.ViewTypeInterface
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.SharedPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class DocsListAdapter(
    private val context: Fragment,
) :
    RecyclerView.Adapter<DocsListAdapter.ViewHolder>() {

    var items: ArrayList<MyModel>? = null
    private var checkList: ArrayList<SelectedModel>? = ArrayList()
    var newItem: MyModel? = null
    private var newPosition = 0
    private val listItem = 0
    private val gridItem = 1
    var isSwitchView = true
    var isLongPress = false

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
            val document = items?.get(position)

            holder.nameHolder.text = items?.get(position)?.title

            if (checkList?.get(position)?.selected == true) {
                isLongPress = true

                holder.clMain.setBackgroundResource(R.color.purple_200)
            } else {
                isLongPress = false

                holder.clMain.setBackgroundResource(android.R.color.transparent)
            }
            holder.itemView.setOnClickListener() {
                if (isLongPress) {
                    longPressCheck(position, holder)
                }
            }
            holder.itemView.setOnLongClickListener {

                longPressCheck(position, holder)

                return@setOnLongClickListener true
            }

            holder.optionHolder.setOnClickListener() {
                val popupMenu = PopupMenu(it.context, holder.optionHolder)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    newPosition = position
                    newItem = document

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

    private fun longPressCheck(position: Int, holder: ViewHolder) {
        val check = checkList?.get(position)?.selected
        val value = checkList?.get(position)?.item

        val prefs = context.context?.let { SharedPrefs.SharedPreferences(it) }

        if (check == true) {

            value?.let { SelectedModel(false, it) }?.let { checkList?.set(position, it) }

            holder.clMain.setBackgroundResource(android.R.color.transparent)
        } else {
            value?.let { SelectedModel(true, it) }?.let { checkList?.set(position, it) }

            holder.clMain.setBackgroundResource(R.color.purple_200)
        }
        for (i in 0 until checkList?.size!!) {
            val check = checkList!![i].selected

            if (check) {
                if (prefs != null) {
                    SharedPrefs.setDocsPrefs(prefs, true)
                }

                isLongPress = true

                (context.context as? ViewTypeInterface)?.setSaveCheckRes(true)
                break
            } else {
                if (prefs != null) {
                    SharedPrefs.setDocsPrefs(prefs, false)
                }
                isLongPress = false

                (context.context as? ViewTypeInterface)?.setSaveCheckRes(false)
            }
        }
        for (i in 0 until checkList?.size!!) {
            val check = checkList!![i].selected

            if (!check) {

                (context.context as? ViewTypeInterface)?.setSelectionCheckRes(false)
            } else {
                (context.context as? ViewTypeInterface)?.setSelectionCheckRes(true)
            }
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

    fun getSelectedItemsCheck(): Boolean {
        var checkVal: Boolean = false
        for (i in 0 until checkList?.size!!) {
            val check = checkList!![i].selected

            if (check) {
                checkVal = check
                break
            }
        }
        return checkVal
    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
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
                        if (currentFile.exists() && newName
                                .isNotEmpty()
                        ) {
                            val newFile = File(
                                currentFile.parentFile,
                                newName + "." + currentFile.extension
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
        newItem: MyModel?, position: Int, newName: String, newFile: File
    ) {

        val finalItem = MyModel(
            newItem?.id,
            newName,
            null,
            newFile.path,
            Uri.fromFile(newFile)
        )
        items?.removeAt(position)
        items?.add(position, finalItem)
        notifyItemChanged(position)
    }

    private fun deleteFunction(view: View, docs: MyModel?) {
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
        checkList?.removeAt(position)
        notifyDataSetChanged()
    }

    fun setListItems(items: ArrayList<MyModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun checkSelectedItems(selected: ArrayList<SelectedModel>) {
        checkList = selected
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