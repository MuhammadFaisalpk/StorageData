package com.example.storage_data.view

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.adapter.DocsListAdapter
import com.example.storage_data.databinding.FragmentDocsBinding
import com.example.storage_data.viewModel.ViewModel


class DocsFragment : Fragment() {

    private lateinit var viewModal: ViewModel
    lateinit var docsListAdapter: DocsListAdapter
    private lateinit var binding: FragmentDocsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

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

        return binding.root
    }

    private fun initViews() {

         recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        docsListAdapter = DocsListAdapter(this)
        recyclerView.adapter = docsListAdapter

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

        viewModal.getAllDocs().observe(viewLifecycleOwner) { list ->
            list?.let {
                //on below line we are updating our list.
                progressBar.visibility = View.GONE
                docsListAdapter.setListItems(it)
            }
        }
    }
}
