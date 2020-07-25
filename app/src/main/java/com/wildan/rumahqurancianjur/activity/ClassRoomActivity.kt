package com.wildan.rumahqurancianjur.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.adapter.PagerAdapterTeacher
import com.wildan.rumahqurancianjur.utils.UtilsConstant
import com.wildan.rumahqurancianjur.utils.UtilsConstant.CLASS_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.LESSON_NAME
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TOOLBAR_TITLE
import com.wildan.rumahqurancianjur.utils.UtilsConstant.USER_ID
import kotlinx.android.synthetic.main.activity_class_room.*

class ClassRoomActivity : AppCompatActivity() {

    private val tabMenu = arrayOf("Beranda", "Santri", "Diskusi")
    private var mUserId: String? = null
    private var mClassId: String? = null
    private var mLessonName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_room)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        mUserId = intent?.getStringExtra(USER_ID).toString()
        mClassId = intent?.getStringExtra(CLASS_ID).toString()
        mLessonName = intent?.getStringExtra(LESSON_NAME).toString()

        val pageAdapter = PagerAdapterTeacher(this)

        view_pager.adapter = pageAdapter

        TabLayoutMediator(
            tab_layout,
            view_pager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = tabMenu[position]
            }).attach()

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fab_create_post.show()
                    else -> fab_create_post.hide()
                }
            }

        })

        fab_create_post.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra(CLASS_ID, mClassId)
            intent.putExtra(LESSON_NAME, mLessonName)
            intent.putExtra(TOOLBAR_TITLE, "Buat Postingan")
            startActivity(intent)
        }
    }
}
