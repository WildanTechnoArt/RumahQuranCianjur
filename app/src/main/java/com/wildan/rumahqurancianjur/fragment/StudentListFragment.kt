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
import com.wildan.rumahqurancianjur.adapter.FirestoreMemberAdapter
import com.wildan.rumahqurancianjur.model.MemberData
import com.wildan.rumahqurancianjur.utils.UtilsConstant
import kotlinx.android.synthetic.main.fragment_student_list.*

class StudentListFragment : Fragment() {

    private lateinit var userId: String
    private lateinit var codeClass: String
    private lateinit var className: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_list, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        codeClass = (context as AppCompatActivity).intent?.getStringExtra(UtilsConstant.CLASS_ID).toString()
        className = (context as AppCompatActivity).intent?.getStringExtra(UtilsConstant.CLASS_NAME).toString()
        setupDatabse()
        getDataCount()
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("classRooms")
            .document(className)
            .collection(userId)
            .document(codeClass)
            .collection("members")
            .orderBy("status")

        val options = FirestoreRecyclerOptions.Builder<MemberData>()
            .setQuery(query, MemberData::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_member_list.layoutManager = LinearLayoutManager(context)
        rv_member_list?.setHasFixedSize(true)

        val adapter = FirestoreMemberAdapter(options)
        rv_member_list?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("classRooms")
            .document(className)
            .collection(userId)
            .document(codeClass)
            .collection("members")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_member_list?.visibility = View.VISIBLE
                tv_nothing_members?.visibility = View.GONE
            } else {
                rv_member_list?.visibility = View.GONE
                tv_nothing_members?.visibility = View.VISIBLE
            }
        }
    }
}