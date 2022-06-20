package com.example.storage_data.view

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
import com.example.storage_data.adapter.DocsListAdapter
import com.example.storage_data.databinding.FragmentDocsBinding
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.*
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


class DocsFragment : Fragment(), Interface, SelectInterface {

    private lateinit var viewModal: ViewModel
    lateinit var docsListAdapter: DocsListAdapter
    private lateinit var binding: FragmentDocsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var docsArray: ArrayList<MyModel>
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()

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
        getAllItemsList()

        return binding.root
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
        docsListAdapter = DocsListAdapter(this)
        recyclerView.adapter = docsListAdapter
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = docsListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = docsListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSaveCheckRes(isSelected)
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

            clearAllSelection()
        }
        viewModal.loadDocs()
    }

    private fun clearAllSelection() {
        arrayCheck?.clear()
        for (item in docsArray) {
            arrayCheck?.add(SelectedModel(false, item))
        }
        docsListAdapter.checkSelectedItems(arrayCheck!!)
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
            for (i in 0 until newArray.size) {
                saveDocumentFile(i, newArray[0].artUri?.path)
            }
        }
        Toast.makeText(context, "Selected files saved.", Toast.LENGTH_SHORT).show()
        unSelectAllItems()

    }

    private fun saveDocumentFile(pos: Int, filePath: String?) {
        val newName = "DOC_${System.currentTimeMillis() + pos}.pdf"

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

        for (item in docsArray!!) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        docsListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(true)

        for (item in docsArray!!) {
            MySingelton.setSelectedImages(item)
        }
    }

    private fun unSelectAllItems() {
        arrayCheck?.clear()

        for (item in docsArray!!) {
            arrayCheck?.add(SelectedModel(false, item))
        }
        docsListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSaveCheckRes(false)

        for (item in docsArray!!) {
            MySingelton.removeSelectedImages(item)
        }
    }
}
