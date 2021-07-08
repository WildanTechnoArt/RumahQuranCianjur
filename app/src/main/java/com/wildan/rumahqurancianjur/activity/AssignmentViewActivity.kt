package com.wildan.rumahqurancianjur.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.adapter.PagerAdapterAssignment
import kotlinx.android.synthetic.main.activity_assignment_view.*

class AssignmentViewActivity : AppCompatActivity() {

    private val tabMenus = arrayListOf("Instruksi", "Pengajuan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_view)
        init()
    }

    private fun init() {
        setSupportActionBar(toolbar)

        val pagerAdapterAssignment = PagerAdapterAssignment(this)

        container.adapter = pagerAdapterAssignment

        TabLayoutMediator(
            tab_layout, container
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()
    }
}