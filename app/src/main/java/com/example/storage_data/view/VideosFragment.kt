package com.example.storage_data.view


import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.MySingelton
import com.example.storage_data.utils.SelectInterface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.viewModel.ViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class VideosFragment : Fragment(), Interface, SelectInterface {

    private lateinit var viewModal: ViewModel
    lateinit var videosListAdapter: VideosListAdapter
    private lateinit var binding: FragmentVideosBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var videosArray: ArrayList<MyModel>
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()

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

        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )

        videosListAdapter = VideosListAdapter(this)
        recyclerView.adapter = videosListAdapter

    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = videosListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = videosListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSelectedDrawableRes(isSelected)
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getVideos().observe(viewLifecycleOwner) { paths ->
            // update UI
            videosArray = paths as ArrayList<MyModel>
            for (item in videosArray) {
                arrayCheck?.add(SelectedModel(false, item))
            }
            progressBar.visibility = View.GONE
            videosListAdapter.setListItems(videosArray)
            videosListAdapter.checkSelectedItems(arrayCheck!!)
        }
        viewModal.loadVideos()

    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = videosListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val getSwitchCheck: Boolean = videosListAdapter.getItemViewType()

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
                saveVideo(newArray[0].artUri?.path, newArray[0].title!!)
            }
        }
    }

    private fun saveVideo(filePath: String?, fileName: String) {
        filePath?.let {
            val context = requireContext()
            val values = ContentValues().apply {
                val folderName = Environment.DIRECTORY_MOVIES
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "video/quicktime")
                if (Build.VERSION.SDK_INT >= 29) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        folderName + "/${context.getString(R.string.app_name)}/"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                } else {
                    val directory = Environment.getExternalStorageDirectory().absolutePath
                        .toString() + File.separator + Environment.DIRECTORY_MOVIES + "/" + getString(
                        R.string.app_name
                    )
                    var createdvideo = File(directory, fileName)
                    put(
                        MediaStore.Video.Media.DATE_ADDED,
                        System.currentTimeMillis() / 1000
                    )
                    put(MediaStore.Video.Media.DATA, createdvideo.absolutePath)

                }
            }

            val fileUri = if (Build.VERSION.SDK_INT >= 29) {
                val collection =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                context.contentResolver.insert(collection, values)
            } else {
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
            fileUri?.let {
                context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                    descriptor?.let {
                        FileOutputStream(descriptor.fileDescriptor).use { out ->
                            val videoFile = File(filePath)
                            FileInputStream(videoFile).use { inputStream ->
                                val buf = ByteArray(8192)
                                while (true) {
                                    val sz = inputStream.read(buf)
                                    if (sz <= 0) break
                                    out.write(buf, 0, sz)
                                }
                            }
                        }
                    }
                }

                values.clear()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(fileUri, values, null, null)
            }
        }
        Toast.makeText(context, "Video saved.", Toast.LENGTH_SHORT).show()
    }

    override fun selectButtonClick() {
        arrayCheck?.clear()

        for (item in videosArray) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        videosListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSelectedDrawableRes(true)

        for (item in videosArray) {
            MySingelton.setSelectedVideos(item)
        }
    }

    override fun unSelectButtonClick() {
        arrayCheck?.clear()

        for (item in videosArray) {
            arrayCheck?.add(SelectedModel(false, item))
        }

        videosListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSelectedDrawableRes(false)

        for (item in videosArray) {
            MySingelton.setSelectedVideos(item)
        }
    }
}
