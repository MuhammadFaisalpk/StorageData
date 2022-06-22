package com.example.storage_data.view

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.adapter.ImagesListAdapter
import com.example.storage_data.databinding.FragmentImagesBinding
import com.example.storage_data.interfaces.SelectionInterface
import com.example.storage_data.interfaces.ViewTypeInterface
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.*
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


class ImagesFragment : Fragment(), SelectionInterface {

    private lateinit var viewModal: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    lateinit var imagesListAdapter: ImagesListAdapter
    private lateinit var binding: FragmentImagesBinding
    private var imagesArray: ArrayList<MyModel>? = ArrayList()
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()
    private var dialog: Dialog? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_images,
            container, false
        )

        initViews()
        getAllItemsList()

        return binding.root
    }

    private fun initViews() {
        sharedPreferences = context?.let { SharedPrefs.SharedPreferences(it) }!!

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )

        imagesListAdapter = ImagesListAdapter(this)
        recyclerView.adapter = imagesListAdapter

        dialog = context?.let { SavingDialog.progressDialog(it) }
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = imagesListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = imagesListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSaveCheckRes(isSelected)

        val prefs = context?.let { SharedPrefs.SharedPreferences(it) }
        if (prefs != null) {
            val sharedVideos = SharedPrefs.getVideosPrefs(prefs)
            val sharedDocs = SharedPrefs.getDocsPrefs(prefs)

            if (sharedVideos || sharedDocs) {
                unSelectAllItems()
            }
        }
    }

    private fun getAllItemsList() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getImages().observe(viewLifecycleOwner) { paths ->
            // update UI
            imagesArray = paths as ArrayList<MyModel>?

            progressBar.visibility = View.GONE
            imagesListAdapter.setListItems(imagesArray!!)

            unSelectAllItems()
        }
        viewModal.loadImages()
    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = imagesListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val getSwitchCheck: Boolean = imagesListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(getSwitchCheck)
    }

    override fun saveButtonClick() {

        val newArray: ArrayList<MyModel>? = ArrayList()

        for (item in arrayCheck!!) {
            if (item.selected) {
                newArray?.add(item.item)
            }
        }

        if (newArray != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    saveImages(newArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun saveImages(
        list: ArrayList<MyModel>,
    ) {
        withContext(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                dialog?.show()
            }
            list.forEachIndexed { index, imageModel ->

                val sourceFile = File(imageModel.path!!)

                val directory =
                    File("${Environment.getExternalStorageDirectory()}/Download/StorageData/")

                if (!directory.exists()) {
                    directory.mkdirs()
                }

                val dFiles = File(
                    directory,
                    sourceFile.name
                )

                try {
                    if (!dFiles.exists()) {
                        dFiles.createNewFile()
                        if (sourceFile.exists()) {
                            copySelectedImages(sourceFile, dFiles)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "onSavedItem:Not Saved ")
                }
            }
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                unSelectAllItems()

                Toast.makeText(context, "Selected files saved.", Toast.LENGTH_SHORT).show()
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

    override fun selectAllButtonClick(selectionCheck: Boolean) {
        if (selectionCheck) {
            selectAllItems()
        } else {
            unSelectAllItems()
        }
    }

    private fun selectAllItems() {
        arrayCheck?.clear()

        for (item in imagesArray!!) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        imagesListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(true)

        SharedPrefs.setImagesPrefs(sharedPreferences, true)

    }

    private fun unSelectAllItems() {
        arrayCheck?.clear()

        for (item in imagesArray!!) {
            arrayCheck?.add(SelectedModel(false, item))
        }
        imagesListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(false)

        SharedPrefs.setImagesPrefs(sharedPreferences, false)

    }

}
