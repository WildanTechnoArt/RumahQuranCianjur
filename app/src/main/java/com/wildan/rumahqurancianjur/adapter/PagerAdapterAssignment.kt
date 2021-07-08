package com.wildan.rumahqurancianjur.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wildan.rumahqurancianjur.fragment.InstructionsFragment
import com.wildan.rumahqurancianjur.fragment.SubmissionsFragment

class PagerAdapterAssignment(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    private val page = listOf(InstructionsFragment(), SubmissionsFragment())

    override fun getItemCount(): Int {
        return page.size
    }

    override fun createFragment(position: Int): Fragment {
        return page[position]
    }
}
