package com.example.storage_data.adapter

import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.VOLUME_INTERNAL
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
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.MySingelton
import com.example.storage_data.utils.ViewTypeInterface
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
                if (!isLongPress) {
                    Toast.makeText(context.context, items?.get(position)?.title, Toast.LENGTH_SHORT)
                        .show()
                } else {
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
//                                mDeleteFunction(it, document)
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

        val sharedPreferences: SharedPreferences? =
            context.context?.getSharedPreferences(
                "kotlinsharedpreference",
                Context.MODE_PRIVATE
            )
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()


        if (check == true) {

            checkList?.removeAt(position)
            checkList?.add(position, value?.let { it1 -> SelectedModel(false, it1) }!!)

            holder.clMain.setBackgroundResource(android.R.color.transparent)
        } else {

            checkList?.removeAt(position)
            checkList?.add(position, value?.let { it1 -> SelectedModel(true, it1) }!!)

            holder.clMain.setBackgroundResource(R.color.purple_200)
        }
        for (i in 0 until checkList?.size!!) {
            val check = checkList!![i].selected

            if (check) {
                editor?.putBoolean("long_press_docs", true)
                editor?.putBoolean("long_press_videos", false)
                editor?.putBoolean("long_press_images", false)

                editor?.apply()

                isLongPress = true

                (context.context as? ViewTypeInterface)?.setSaveCheckRes(true)
                break
            } else {
                editor?.putBoolean("long_press_docs", false)

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
        newItem: MyModel?, position: Int, newName: String, newFile: File
    ) {

        val newItem = MyModel(
            newItem?.id,
            newName,
            null,
            newFile.path,
            Uri.fromFile(newFile)
        )
        items?.removeAt(position)
        items?.add(position, newItem)
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

    private fun mDeleteFunction(v: View?, docs: MyModel?) {
        val contentResolver = context.requireContext().contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //list of docs to delete
            val uri = docs?.id?.let {
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.getContentUri(
                        VOLUME_INTERNAL
                    ), it.toLong()
                )
            }

            val uriList: List<Uri> = listOf(uri) as List<Uri>
            //requesting for delete permission
            val pi =
                MediaStore.createDeleteRequest(
                    contentResolver,
                    uriList
                )
            if (v != null) {
                (v.context as Activity).startIntentSenderForResult(
                    pi.intentSender, 127,
                    null, 0, 0, 0, null
                )
            }
        } else {
            //for devices less than android 11
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
//        notifyItemChanged(position)
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