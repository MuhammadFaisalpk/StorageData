package com.example.storage_data.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storage_data.R
import com.example.storage_data.adapter.DocsListAdapter
import com.example.storage_data.databinding.FragmentDocsBinding
import com.example.storage_data.model.Documents
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.viewModel.ViewModel


class DocsFragment : Fragment(), Interface {

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

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
         val resolutionForResult =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                //do whatever you want with activity result...
            }
        docsListAdapter = DocsListAdapter(this,resolutionForResult)
        recyclerView.adapter = docsListAdapter
    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = docsListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setDrawableRes(isSwitched)
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getDocs().observe(viewLifecycleOwner) { paths ->
            // update UI
            progressBar.visibility = View.GONE
            docsListAdapter.setListItems(paths as ArrayList<Documents>)
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
        (activity as? ViewTypeInterface)?.setDrawableRes(getSwitchCheck)
    }
}
