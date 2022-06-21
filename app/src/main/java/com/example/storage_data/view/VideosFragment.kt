package com.example.storage_data.view


import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.adapter.VideosListAdapter
import com.example.storage_data.databinding.FragmentVideosBinding
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.*
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import kotlin.collections.ArrayList


class VideosFragment : Fragment(), SelectionInterface {

    private lateinit var viewModal: ViewModel
    lateinit var videosListAdapter: VideosListAdapter
    private lateinit var binding: FragmentVideosBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var videosArray: ArrayList<MyModel>? = ArrayList()
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()
    private var dialog: Dialog? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_videos,
            container, false
        )

        initViews()
        getAllItemsList()

        return binding.root
    }

    private fun initViews() {
        sharedPreferences =
            context?.getSharedPreferences(
                "kotlinsharedpreference",
                Context.MODE_PRIVATE
            )!!

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )

        videosListAdapter = VideosListAdapter(this)
        recyclerView.adapter = videosListAdapter

        dialog = context?.let { SavingDialog.progressDialog(it) }
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = videosListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = videosListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSaveCheckRes(isSelected)

        val sharedImages = sharedPreferences.getBoolean("long_press_images", false)
        val sharedDocs = sharedPreferences.getBoolean("long_press_docs", false)

        if (sharedImages || sharedDocs) {
            unSelectAllItems()
        }
    }

    private fun getAllItemsList() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getVideos().observe(viewLifecycleOwner) { paths ->
            // update UI
            videosArray = paths as ArrayList<MyModel>

            progressBar.visibility = View.GONE
            videosListAdapter.setListItems(videosArray!!)

            unSelectAllItems()
        }
        viewModal.loadVideos()

    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = videosListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val getSwitchCheck: Boolean = videosListAdapter.getItemViewType()

        (activity as? ViewTypeInterface)?.setGridDrawableRes(getSwitchCheck)
    }

    override fun saveButtonClick() {
        var newArray: ArrayList<MyModel>? = ArrayList()

        for (item in arrayCheck!!) {
            if (item.selected) {
                newArray?.add(item.item)
            }
        }
        if (newArray != null) {

            dialog?.show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    saveVideos(newArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            unSelectAllItems()
        }
    }

    private suspend fun saveVideos(
        list: ArrayList<MyModel>,
    ) {
        withContext(Dispatchers.IO) {
            list.forEachIndexed { index, imageModel ->

                val sourceFile = File(imageModel.path!!)

                val directory =
                    File("${Environment.getExternalStorageDirectory()}/Download/StorageData/")

                val dFiles = File(
                    directory,
                    sourceFile.name
                )
                if (!directory.exists()) {
                    directory.mkdirs()
                }

                try {
                    if (sourceFile.exists()) {

                        copySelectedImages(sourceFile, dFiles)

                    }
                } catch (e: Exception) {
                    Log.e("TAG", "onSavedItem:Not Saved ")
                }
                if (index == list.size - 1) {
                    dialog?.dismiss()
                }
            }
        }
    }

    private fun copySelectedImages(src: File, dest: File) {
        FileInputStream(src).use { fis ->
            FileOutputStream(dest).use { os ->
                val buffer = ByteArray(1024)
                var len: Int
                while (fis.read(buffer).also { len = it } != -1) {
                    os.write(buffer, 0, len)
                }
                MediaScannerConnection.scanFile(
                    context, arrayOf(dest.absolutePath), null
                ) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                }
            }
        }
    }

    private fun saveVideoFile(pos: Int, filePath: String?) {
        val newName = "VID_${System.currentTimeMillis() + pos}.mp4"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentFile = filePath?.let { File(it) }

                val wallpaperDirectory =
                    File("${Environment.getExternalStorageDirectory()}/Download/StorageData/")

                val newFile = File(
                    wallpaperDirectory,
                    newName
                )
                if (!wallpaperDirectory.exists()) {
                    wallpaperDirectory.mkdirs()
                }
                if (currentFile != null) {
                    if (currentFile.exists()) {
                        val `in`: InputStream = FileInputStream(currentFile)
                        val out: OutputStream = FileOutputStream(newFile)

                        // Copy the bits from instream to outstream
                        val buf = ByteArray(1024)
                        var len: Int
                        while (`in`.read(buf).also { len = it } > 0) {
                            out.write(buf, 0, len)
                        }
                        `in`.close()
                        out.close()

                        MediaScannerConnection.scanFile(
                            context, arrayOf(newFile.absolutePath), null
                        ) { path, uri ->
                            Log.i("ExternalStorage", "Scanned $path:")
                            Log.i("ExternalStorage", "-> uri=$uri")
                        }

                        Log.i("ExternalStorage", "Video Saved.")

                    } else {
                        Log.i("ExternalStorage", "Video saving failed.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//        Toast.makeText(context, "$newName saved.", Toast.LENGTH_SHORT).show()
    }

    override fun selectButtonClick(selectionCheck: Boolean) {
        if (selectionCheck) {
            selectAllItems()
        } else {
            unSelectAllItems()
        }
    }

    private fun selectAllItems() {
        arrayCheck?.clear()

        for (item in videosArray!!) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        videosListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(true)

        val editor: SharedPreferences.Editor? = sharedPreferences.edit()
        editor?.putBoolean("long_press_images", false)
        editor?.putBoolean("long_press_videos", true)
        editor?.putBoolean("long_press_docs", false)
        editor?.apply()
    }

    private fun unSelectAllItems() {
        arrayCheck?.clear()

        for (item in videosArray!!) {
            arrayCheck?.add(SelectedModel(false, item))
        }
        videosListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(false)
    }
}