package com.example.storage_data.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.storage_data.R
import com.example.storage_data.databinding.ActivityMainBinding
import com.example.storage_data.utils.Interface
import com.example.storage_data.utils.MySingelton
import com.example.storage_data.utils.SelectInterface
import com.example.storage_data.utils.ViewTypeInterface
import com.example.storage_data.view.DocsFragment
import com.example.storage_data.view.ImagesFragment
import com.example.storage_data.view.SavedFragment
import com.example.storage_data.view.VideosFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity(), ViewTypeInterface {

    private lateinit var binding: ActivityMainBinding
    private lateinit var layout: View
    lateinit var viewPager: ViewPager2
    private lateinit var gridChange: ImageView
    lateinit var selectAll: TextView
    private lateinit var unSelectAll: TextView
    lateinit var save: TextView

    private lateinit var tabLayout: TabLayout
    private val WRITE_STORAGE_PERMISSION_REQUEST_CODE = 13
    private val adapter = AdapterTabPager(this)
    private lateinit var currentFragment: Fragment

    private var listFragments = arrayOf(
        ImagesFragment(), VideosFragment(),
        DocsFragment(), SavedFragment()
    )

    private val fragmentNames = arrayOf(
        "Images", "Videos",
        "Docs", "Saved"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

        if (checkPermission()) {
            setStatePageAdapter()
        } else {
            requestPermission()
        }

        tabLayoutListener()
    }

    private fun initViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        layout = binding.root

        viewPager = binding.pager
        tabLayout = binding.tabs
        gridChange = binding.gridChange
        selectAll = binding.selectAll
        unSelectAll = binding.unselectAll
        save = binding.save

        gridChange.setOnClickListener() {
            currentFragment = listFragments[viewPager.currentItem] as Fragment
            (currentFragment as Interface).gridButtonClick()
        }
        selectAll.setOnClickListener() {
            currentFragment = listFragments[viewPager.currentItem] as Fragment
            (currentFragment as SelectInterface).selectButtonClick()
        }
        unSelectAll.setOnClickListener() {
            currentFragment = listFragments[viewPager.currentItem] as Fragment
            (currentFragment as SelectInterface).unSelectButtonClick()
        }
        save.setOnClickListener() {
            currentFragment = listFragments[viewPager.currentItem] as Fragment
            (currentFragment as Interface).saveButtonClick()
        }
    }

    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readStorage =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val writeStorage =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            readStorage == PackageManager.PERMISSION_GRANTED &&
                    writeStorage == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName");
            startActivityForResult(intent, 2296)
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE
                ),
                WRITE_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setStatePageAdapter() {
        for (item in listFragments.indices) {
            adapter.addFragment(listFragments[item] as Fragment, fragmentNames[item])
        }
        viewPager.adapter = adapter
        viewPager.currentItem = 0

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

    private fun tabLayoutListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position;
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
    }

    class AdapterTabPager(activity: FragmentActivity?) : FragmentStateAdapter(activity!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()

        fun getTabTitle(position: Int): String {
            return mFragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getItemCount(): Int {
            return mFragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setStatePageAdapter()
            } else {
                showSnackBar(
                    R.string.storage_permission_check,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.ok,
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED && requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    setStatePageAdapter()
                } else {
                    showSnackBar(
                        R.string.storage_permission_check,
                        Snackbar.LENGTH_INDEFINITE,
                        R.string.ok,
                    )
                }
            }
        } else if (resultCode == RESULT_OK) {
            when (requestCode) {
                123, 124 -> {
                    val fragment = getVisibleFragment() as VideosFragment
                    fragment.videosListAdapter.onResult(requestCode)
                }
                125, 126 -> {
                    val fragment = getVisibleFragment() as ImagesFragment
                    fragment.imagesListAdapter.onResult(requestCode)
                }
                127, 128 -> {
                    val fragment = getVisibleFragment() as DocsFragment
                    fragment.docsListAdapter.onResult(requestCode)
                }
            }
        }
    }

    private fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible) return fragment
        }
        return null
    }

    private fun showSnackBar(
        permissionCheck: Int,
        lengthLong: Int,
        actionText: Int,
    ) {
        val snackBar =
            Snackbar.make(layout, permissionCheck, lengthLong)
                .setAction(actionText) {
                    requestPermission()
                }
        snackBar.show()
    }

    override fun setGridDrawableRes(enabled: Boolean) {
        if (enabled) gridChange.setImageResource(R.drawable.ic_baseline_grid_on)
        else gridChange.setImageResource(R.drawable.ic_baseline_grid_off)
    }

    override fun setSelectedDrawableRes(enabled: Boolean) {
        if (enabled) save.visibility = View.VISIBLE
        else save.visibility = View.GONE
    }
}