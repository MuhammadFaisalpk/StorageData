package com.example.storage_data

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
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.storage_data.databinding.ActivityMainBinding
import com.example.storage_data.view.DocsFragment
import com.example.storage_data.view.ImagesFragment
import com.example.storage_data.view.VideosFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var layout: View
    lateinit var viewPager: ViewPager2
    lateinit var gridChange: ImageView

    private lateinit var tabLayout: TabLayout
    private val WRITE_STORAGE_PERMISSION_REQUEST_CODE = 13
    private val adapter = AdapterTabPager(this)
    private lateinit var currentFragment: Fragment

    private val listofFragment = arrayOf(
        ImagesFragment(),
        VideosFragment(), DocsFragment()
    )

    private val fragmentNames = arrayOf(
        "Images",
        "Videos", "Docs"
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

        gridChange.setOnClickListener() {
            currentFragment = listofFragment[viewPager.currentItem]
            when (currentFragment) {
                is ImagesFragment -> (currentFragment as ImagesFragment).gridChangeMethod()
                is VideosFragment -> (currentFragment as VideosFragment).gridChangeMethod()
                is DocsFragment -> (currentFragment as DocsFragment).gridChangeMethod()
            }
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
        for (item in listofFragment.indices) {
            adapter.addFragment(listofFragment[item], fragmentNames[item])
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
                viewPager.currentItem = tab.position
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                val count = fm.backStackEntryCount
                if (count >= 1) {
                    supportFragmentManager.popBackStack()
                }
                ft.commit()

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
                    requestCode
                )
            }
        }
//        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setStatePageAdapter()
//            } else {
//                showSnackBar(
//                    R.string.storage_permission_check,
//                    Snackbar.LENGTH_INDEFINITE,
//                    R.string.ok,
//                    requestCode
//                )
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
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
                2296 -> {
                    if (SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            setStatePageAdapter()
                        } else {
                            showSnackBar(
                                R.string.storage_permission_check,
                                Snackbar.LENGTH_INDEFINITE,
                                R.string.ok,
                                requestCode
                            )
                        }
                    }
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
        requestCode: Int
    ) {
        val snackBar =
            Snackbar.make(layout, permissionCheck, lengthLong)
                .setAction(actionText) {
                    requestPermission()
                }
        snackBar.show()
    }
}