package com.example.storage_data.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.adapter.DocsListAdapter
import com.example.storage_data.databinding.FragmentDocsBinding
import com.example.storage_data.interfaces.SelectionInterface
import com.example.storage_data.interfaces.ViewTypeInterface
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.SavingDialog
import com.example.storage_data.utils.SharedPrefs
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class DocsFragment : Fragment(), SelectionInterface {

    private lateinit var viewModal: ViewModel
    lateinit var docsListAdapter: DocsListAdapter
    private lateinit var binding: FragmentDocsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var docsArray: ArrayList<MyModel>
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()
    private var dialog: Dialog? = null
    private lateinit var sharedPreferences: SharedPreferences

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted) {
                Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_docs,
            container, false
        )

        initViews()
        val appPerms = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

//        activityResultLauncher.launch(appPerms)

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
        docsListAdapter = DocsListAdapter(this)
        recyclerView.adapter = docsListAdapter

        dialog = context?.let { SavingDialog.progressDialog(it) }
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = docsListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = docsListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSaveCheckRes(isSelected)

        val prefs = context?.let { SharedPrefs.SharedPreferences(it) }
        if (prefs != null) {
            val sharedImages = SharedPrefs.getImagesPrefs(prefs)
            val sharedVideos = SharedPrefs.getVideosPrefs(prefs)

            if (sharedImages || sharedVideos) {
                unSelectAllItems()
            }
        }
    }

    private fun getAllItemsList() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getDocs().observe(viewLifecycleOwner) { paths ->
            // update UI
            docsArray = paths as ArrayList<MyModel>

            progressBar.visibility = View.GONE
            docsListAdapter.setListItems(docsArray)

            unSelectAllItems()
        }
        viewModal.loadDocs()
    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = docsListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val getSwitchCheck: Boolean = docsListAdapter.getItemViewType()
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


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    saveDocs(newArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun saveDocs(
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
                            copySelectedDocs(sourceFile, dFiles)
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

    private fun copySelectedDocs(src: File, dest: File) {
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

        for (item in docsArray!!) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        docsListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(true)

        SharedPrefs.setDocsPrefs(sharedPreferences, true)
    }

    private fun unSelectAllItems() {
        arrayCheck?.clear()

        for (item in docsArray!!) {
            arrayCheck?.add(SelectedModel(false, item))
        }
        docsListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(false)

        SharedPrefs.setDocsPrefs(sharedPreferences, false)
    }
}
