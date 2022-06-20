package com.example.storage_data.view

import android.os.Bundle
import android.os.Environment
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
import com.example.storage_data.adapter.SavedListAdapter
import com.example.storage_data.databinding.FragmentDocsBinding
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SavedModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SavedFragment : Fragment(), Interface {

    private lateinit var viewModal: ViewModel
    lateinit var savedListAdapter: SavedListAdapter
    private lateinit var binding: FragmentDocsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var filesArray: ArrayList<SavedModel>? = ArrayList()

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
        getAllSaved()
//        showImageList()

        return binding.root
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
        savedListAdapter = SavedListAdapter(this)
        recyclerView.adapter = savedListAdapter
    }

    private fun getAllSaved() {
        filesArray?.clear()

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getSaved().observe(viewLifecycleOwner) { paths ->
            // update UI
            filesArray = paths as ArrayList<SavedModel>?

            progressBar.visibility = View.GONE
            filesArray?.let { savedListAdapter.setListItems(it) }
        }
        viewModal.loadSaved()
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = savedListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)
    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = savedListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val getSwitchCheck: Boolean = savedListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(getSwitchCheck)
    }

    override fun saveButtonClick() {
        TODO("Not yet implemented")
    }
}
