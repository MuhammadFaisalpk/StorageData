package com.example.storage_data.view

import android.graphics.drawable.Drawable
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.storage_data.R
import com.example.storage_data.adapter.ImagesListAdapter
import com.example.storage_data.databinding.FragmentImagesBinding
import com.example.storage_data.model.Images
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.*


class ImagesFragment : Fragment(), Interface {

    private lateinit var viewModal: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    lateinit var imagesListAdapter: ImagesListAdapter
    private lateinit var binding: FragmentImagesBinding
    private lateinit var array: ArrayList<Images>

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
        getAllItems()

        return binding.root
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )

        imagesListAdapter = ImagesListAdapter(this)
        recyclerView.adapter = imagesListAdapter

    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = imagesListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setDrawableRes(isSwitched)
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

//        viewModal.getAllImages().observe(viewLifecycleOwner) { list ->
//            list?.let {
//                array = list
//                progressBar.visibility = View.GONE
//                imagesListAdapter.setListItems(array)
//            }
//        }
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                viewModal.getAllImages().observe(viewLifecycleOwner) { list ->
                    list?.let {
                        array = list
                        // Call to UI thread
                        progressBar.visibility = View.GONE
                        imagesListAdapter.setListItems(array)
                    }
                }
            }
        }
    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = imagesListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val isSwitched1: Boolean = imagesListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setDrawableRes(isSwitched1)
    }
}
