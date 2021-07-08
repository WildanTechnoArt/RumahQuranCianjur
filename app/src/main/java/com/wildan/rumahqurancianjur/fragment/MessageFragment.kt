package com.wildan.rumahqurancianjur.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.adapter.TeacherChatListAdapter
import com.wildan.rumahqurancianjur.model.ListChat
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class MessageFragment : Fragment() {

    private lateinit var mUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        prepare(view)
        setupDatabse()
        getDataCount()
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("users")
            .document("chat")
            .collection(mUserId)

        val options = FirestoreRecyclerOptions.Builder<ListChat>()
            .setQuery(query, ListChat::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = TeacherChatListAdapter(options)
        rv_class_list?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("users")
            .document("chat")
            .collection(mUserId)

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_class_list?.visibility = View.VISIBLE
                tv_no_message?.visibility = View.GONE
            } else {
                rv_class_list?.visibility = View.GONE
                tv_no_message?.visibility = View.VISIBLE
            }
        }
    }

    private fun prepare(view: View) {
        (view.context as AppCompatActivity).setSupportActionBar(toolbar)
        (view.context as AppCompatActivity).supportActionBar?.title = "Pesan"

        mUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        rv_class_list?.layoutManager = LinearLayoutManager(context)
        rv_class_list?.setHasFixedSize(true)
    }
}
