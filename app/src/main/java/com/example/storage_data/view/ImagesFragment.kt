package com.example.storage_data.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.adapter.ImagesListAdapter
import com.example.storage_data.databinding.FragmentImagesBinding
import com.example.storage_data.viewModel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ImagesFragment : Fragment() {

    private lateinit var viewModal: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    lateinit var imagesListAdapter: ImagesListAdapter
    private lateinit var binding: FragmentImagesBinding

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

        // Launch a coroutine that by default goes to the main thread
        GlobalScope.launch(Dispatchers.Main) {
            getAllItems()
        }

        return binding.root
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        imagesListAdapter = ImagesListAdapter(this)
        recyclerView.adapter = imagesListAdapter

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        ).get(ViewModel::class.java)

        viewModal.getAllImages().observe(viewLifecycleOwner) { list ->
            list?.let {
                //on below line we are updating our list.
                progressBar.visibility = View.GONE
                imagesListAdapter.setListItems(it)
            }
        }
    }
}
