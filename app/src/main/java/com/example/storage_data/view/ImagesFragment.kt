package com.example.storage_data.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
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
import com.example.storage_data.adapter.ImagesListAdapter
import com.example.storage_data.databinding.FragmentImagesBinding
import com.example.storage_data.model.MyModel
import com.example.storage_data.model.SelectedModel
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.MySingelton
import com.example.storage_data.utils.SelectInterface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.viewModel.ViewModel
import java.io.*
import java.util.*


class ImagesFragment : Fragment(), Interface, SelectInterface {

    private lateinit var viewModal: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    lateinit var imagesListAdapter: ImagesListAdapter
    private lateinit var binding: FragmentImagesBinding
    private lateinit var imagesArray: ArrayList<MyModel>
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()

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

        imagesListAdapter = ImagesListAdapter(this, this)
        recyclerView.adapter = imagesListAdapter

    }

    override fun onResume() {
        super.onResume()

        val isSwitched: Boolean = imagesListAdapter.getItemViewType()
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = imagesListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSelectedDrawableRes(isSelected)
    }

    private fun getAllItems() {
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getImages().observe(viewLifecycleOwner) { paths ->
            // update UI
            imagesArray = paths as ArrayList<MyModel>
            for (item in imagesArray) {
                arrayCheck?.add(SelectedModel(false, item))
            }
            progressBar.visibility = View.GONE
            imagesListAdapter.setListItems(imagesArray)
            imagesListAdapter.checkSelectedItems(arrayCheck!!)

        }
        viewModal.loadImages()
    }

    override fun gridButtonClick() {
        val isSwitched: Boolean = imagesListAdapter.toggleItemViewType()
        recyclerView.layoutManager =
            if (isSwitched) LinearLayoutManager(context) else GridLayoutManager(
                context,
                3
            )
        val getSwitchCheck: Boolean = imagesListAdapter.getItemViewType()
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
                val bitmap: Bitmap = getThumbnail(newArray[i].artUri)!!
                saveImage(bitmap)
            }
        }
    }


    private fun getThumbnail(uri: Uri?): Bitmap? {
        var input: InputStream? = uri?.let { context?.contentResolver?.openInputStream(it) }
        val onlyBoundsOptions = BitmapFactory.Options()
        onlyBoundsOptions.inJustDecodeBounds = true
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
        input?.close()
        if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
            return null
        }
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        input = uri?.let { context?.contentResolver?.openInputStream(it) }
        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input?.close()
        return bitmap
    }

    private fun saveImage(data: Bitmap) {
        val createFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            getString(R.string.app_name) + "/Saved"
        )
        if (!createFolder.exists()) createFolder.mkdir()
        val saveImage = File(createFolder, "SD-${System.currentTimeMillis()}.jpg")
        try {
            val outputStream: OutputStream = FileOutputStream(saveImage)
            data.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            MediaScannerConnection.scanFile(
                context, arrayOf(saveImage.absolutePath), null
            ) { path, uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(context, "Image saved", Toast.LENGTH_SHORT).show()
    }

    override fun selectButtonClick() {
        arrayCheck?.clear()

        for (item in imagesArray) {
            arrayCheck?.add(SelectedModel(true, item))
        }

        imagesListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSelectedDrawableRes(true)

        for (item in imagesArray) {
            MySingelton.setSelectedImages(item)
        }
    }

    override fun unSelectButtonClick() {
        arrayCheck?.clear()

        for (item in imagesArray) {
            arrayCheck?.add(SelectedModel(false, item))
        }
        imagesListAdapter.checkSelectedItems(arrayCheck!!)

        (activity as? ViewTypeInterface)?.setSelectedDrawableRes(false)

        for (item in imagesArray) {
            MySingelton.removeSelectedImages(item)
        }
    }
}
