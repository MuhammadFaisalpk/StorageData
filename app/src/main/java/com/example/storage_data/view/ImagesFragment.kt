package com.example.storage_data.view

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


class ImagesFragment : Fragment(), Interface, SelectInterface {

    private lateinit var viewModal: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    lateinit var imagesListAdapter: ImagesListAdapter
    private lateinit var binding: FragmentImagesBinding
    private var imagesArray: ArrayList<MyModel>? = ArrayList()
    private var arrayCheck: ArrayList<SelectedModel>? = ArrayList()
    private lateinit var dialog: AlertDialog

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
        (activity as? ViewTypeInterface)?.setGridDrawableRes(isSwitched)

        val isSelected: Boolean = imagesListAdapter.getSelectedItemsCheck()
        (activity as? ViewTypeInterface)?.setSaveCheckRes(isSelected)
    }

    private fun getAllItems() {
        imagesArray?.clear()
        arrayCheck?.clear()

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity?.application!!)
        )[ViewModel::class.java]

        viewModal.getImages().observe(viewLifecycleOwner) { paths ->
            // update UI
            imagesArray = paths as ArrayList<MyModel>?
            for (item in imagesArray!!) {
                arrayCheck?.add(SelectedModel(false, item))
            }
            progressBar.visibility = View.GONE
            imagesListAdapter.setListItems(imagesArray!!)
            imagesListAdapter.checkSelectedItems(arrayCheck!!)

        }
        viewModal.loadImages()
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
        val newName = "IMG_${System.currentTimeMillis()}.jpg"
        CoroutineScope(Dispatchers.IO).launch {
            val createFolder =
                File("${Environment.getExternalStorageDirectory()}/Download/StorageData/")
            if (!createFolder.exists()) createFolder.mkdir()
            val saveImage = File(createFolder, newName)
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
        }
//        Toast.makeText(context, "$newName saved.", Toast.LENGTH_SHORT).show()
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

    fun setProgressDialog() {

        // Creating a Linear Layout
        val llPadding = 30
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(llPadding, llPadding, llPadding, llPadding)
        linearLayout.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        linearLayout.layoutParams = llParam

        // Creating a ProgressBar inside the layout
        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER

        // Creating a TextView inside the layout
        val tvText = TextView(context)
        tvText.text = "Saving files ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        linearLayout.addView(progressBar)
        linearLayout.addView(tvText)

        // Setting the AlertDialog Builder view
        // as the Linear layout created above
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(linearLayout)

        // Displaying the dialog
        dialog = builder.create()

        val window: Window? = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams

            // Disabling screen touch to avoid exiting the Dialog
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }
    }

    override fun saveButtonClick() {

        var newArray: ArrayList<MyModel>? = ArrayList()

        for (item in arrayCheck!!) {
            if (item.selected) {
                newArray?.add(item.item)
            }
        }
        if (newArray != null) {

//            setProgressDialog()
//            dialog.show()

            for (i in 0 until newArray.size) {
                val bitmap: Bitmap = getThumbnail(newArray[i].artUri)!!
                saveImage(bitmap)
            }
            Toast.makeText(context, "Selected files saved.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun selectButtonClick(selectionCheck: Boolean) {
        if (selectionCheck) {
            arrayCheck?.clear()

            for (item in imagesArray!!) {
                arrayCheck?.add(SelectedModel(true, item))
            }

            imagesListAdapter.checkSelectedItems(arrayCheck!!)

            (activity as? ViewTypeInterface)?.setSaveCheckRes(true)

            for (item in imagesArray!!) {
                MySingelton.setSelectedImages(item)
            }
        } else {
            arrayCheck?.clear()

            for (item in imagesArray!!) {
                arrayCheck?.add(SelectedModel(false, item))
            }
            imagesListAdapter.checkSelectedItems(arrayCheck!!)

            (activity as? ViewTypeInterface)?.setSaveCheckRes(false)

            for (item in imagesArray!!) {
                MySingelton.removeSelectedImages(item)
            }
        }
    }
}
