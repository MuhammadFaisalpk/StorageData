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
import com.example.storage_data.adapter.ImagesListAdapter
import com.example.storage_data.databinding.FragmentImagesBinding
import com.example.storage_data.utils.DeleteInterface
import com.example.storage_data.viewModel.ViewModel

class ImagesFragment : Fragment(), DeleteInterface {

    private lateinit var viewModal: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    var imagesListAdapter: ImagesListAdapter = ImagesListAdapter(this)
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
        getAllItems()

        return binding.root
    }

    fun gridChangeMethod() {
        val isSwitched: Boolean = imagesListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                2
            )
//        imagesListAdapter.notifyDataSetChanged()
    }

    private fun initViews() {

        recyclerView = binding.recyclerView
        progressBar = binding.progressBar

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )

//        imagesListAdapter = ImagesListAdapter(this)
        recyclerView.adapter = imagesListAdapter
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

    override fun gridButtonClick(currentFragment: Fragment) {
        Toast.makeText(currentFragment.context, "Done $currentFragment", Toast.LENGTH_SHORT)
            .show()
//        val isSwitched: Boolean = imagesListAdapter.toggleItemViewType()
//        recyclerView.layoutManager =
//            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
//                context,
//                2
//            )
//        imagesListAdapter.notifyDataSetChanged()
    }
}
