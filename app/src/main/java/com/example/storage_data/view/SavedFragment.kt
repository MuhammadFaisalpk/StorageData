package com.example.storage_data.view

import android.R.attr.path
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.SelectInterface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.viewModel.ViewModel
import java.io.File


class SavedFragment : Fragment(), Interface, SelectInterface {

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
        getAllItems()
        showImageList()

        return binding.root
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
//        docsListAdapter = DocsListAdapter(this)
//        recyclerView.adapter = docsListAdapter
    }

    private fun showImageList() {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            getString(R.string.app_name) + "/Saved"
        )

        if (file.exists()) {
            val filesList: Array<out File>? = file.listFiles()

            if (filesList != null) {
                Log.d("dasd", filesList?.size.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = docsListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getDocs().observe(viewLifecycleOwner) { paths ->
            // update UI
            docsArray = paths as ArrayList<MyModel>

            for (item in docsArray) {
                arrayCheck?.add(SelectedModel(false, item))
            }

            progressBar.visibility = View.GONE
            docsListAdapter.setListItems(docsArray)
            docsListAdapter.checkSelectedItems(arrayCheck!!)
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
        TODO("Not yet implemented")
    }

    override fun selectButtonClick() {
        arrayCheck?.clear()

        for (item in docsArray) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        docsListAdapter.checkSelectedItems(arrayCheck!!)
    }

    override fun unSelectButtonClick() {
        arrayCheck?.clear()

        for (item in docsArray) {
            arrayCheck?.add(SelectedModel(false, item))
        }

        docsListAdapter.checkSelectedItems(arrayCheck!!)
    }
}
