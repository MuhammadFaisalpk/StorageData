package com.example.storage_data.view

import android.os.Bundle
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
import com.example.storage_data.viewModel.ViewModel


class VideosFragment : Fragment() {

    private lateinit var viewModal: ViewModel
    lateinit var videosListAdapter: VideosListAdapter
    private lateinit var binding: FragmentVideosBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

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
        getAllItems()

        return binding.root
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        videosListAdapter = VideosListAdapter(this)
        recyclerView.adapter = videosListAdapter

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
    }

    fun gridChangeMethod() {
        val isSwitched: Boolean = videosListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                2
            )
//        imagesListAdapter.notifyDataSetChanged()
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        ).get(ViewModel::class.java)

        viewModal.getAllVideos().observe(viewLifecycleOwner) { list ->
            list?.let {
                //on below line we are updating our list.
                progressBar.visibility = View.GONE
                videosListAdapter.setListItems(list)
            }
        }
    }
}
