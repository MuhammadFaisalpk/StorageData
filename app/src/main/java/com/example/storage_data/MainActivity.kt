package com.example.storage_data

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
    private lateinit var tabLayout: TabLayout
    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 14
    private val WRITE_STORAGE_PERMISSION_REQUEST_CODE = 13
    val adapter = AdapterTabPager(this)

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
        if (requestRuntimePermission()) {
            setStatePageAdapter()
        }
        tabLayoutListener()
    }

    private fun initViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        layout = binding.root

        viewPager = binding.pager
        tabLayout = binding.tabs
    }

    //for requesting permission
    private fun requestRuntimePermission(): Boolean {
        //requesting storage permission for only devices less than api 28
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE
                )
                return false
            }
        } else {
            //read external storage permission for devices higher than android 10 i.e. api 29
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_REQUEST_CODE
                )
                return false
            }
        }
        return true
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
        //for read external storage permission
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 123 || requestCode == 124) {
                var fragment = getVisibleFragment() as VideosFragment
                fragment.videosListAdapter.onResult(requestCode)
            } else if (requestCode == 125 || requestCode == 126) {
                var fragment = getVisibleFragment() as ImagesFragment
                fragment.imagesListAdapter.onResult(requestCode)
            } else if (requestCode == 127 || requestCode == 128) {
                var fragment = getVisibleFragment() as DocsFragment
                fragment.docsListAdapter.onResult(requestCode)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            requestCode
                        )
                    }
                }
        snackBar.show()
    }
}