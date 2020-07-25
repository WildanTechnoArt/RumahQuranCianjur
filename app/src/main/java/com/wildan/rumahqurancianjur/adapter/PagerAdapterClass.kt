package com.wildan.rumahqurancianjur.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wildan.rumahqurancianjur.fragment.ChatGroupFragment
import com.wildan.rumahqurancianjur.fragment.ClassPostFragment
import com.wildan.rumahqurancianjur.fragment.StudentListFragment

class PagerAdapterTeacher(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(ClassPostFragment(), StudentListFragment(), ChatGroupFragment())

    //Mengembalikan Fragment yang terkait dengan posisi tertentu
    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }

    //Mengembalikan jumlah tampilan yang tersedia.
    override fun getItemCount(): Int {
        return pages.size
    }
}