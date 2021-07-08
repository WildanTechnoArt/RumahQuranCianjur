package com.wildan.rumahqurancianjur.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.activity.ClassRoomActivity
import com.wildan.rumahqurancianjur.adapter.FirestoreClassAdapter
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.interfaces.ClassListListener
import com.wildan.rumahqurancianjur.model.ClassModel
import com.wildan.rumahqurancianjur.utils.UtilsConstant.CLASS_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.USER_ID
import kotlinx.android.synthetic.main.fragment_list_class.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ClassListFragment : Fragment(), ClassListListener {

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        checkClass()
    }

    private fun prepare(view: View) {
        userId = SharedPrefManager.getInstance(view.context).getUserId.toString()

        (view.context as AppCompatActivity).setSupportActionBar(toolbar)
        (view.context as AppCompatActivity).supportActionBar?.title = "Daftar Kelas"

        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("classList")

        val options = FirestoreRecyclerOptions.Builder<ClassModel>()
            .setQuery(query, ClassModel::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_class_list?.layoutManager = LinearLayoutManager(context)
        rv_class_list?.setHasFixedSize(true)

        val adapter = context?.let { FirestoreClassAdapter(it, options, this) }
        rv_class_list?.adapter = adapter
    }

    private fun checkClass() {
        val db = FirebaseFirestore.getInstance()
            .collection("classList")
        db.addSnapshotListener { snapshot, _ ->
            if (snapshot?.isEmpty == true) {
                swipe_refresh?.visibility = View.GONE
                tv_no_class?.visibility = View.VISIBLE
            } else {
                swipe_refresh?.visibility = View.VISIBLE
                tv_no_class?.visibility = View.GONE
                requestData()
            }

            swipe_refresh?.isRefreshing = false
        }
    }

    override fun onClick(userId: String, classId: String) {
        val intent = Intent(context, ClassRoomActivity::class.java).also {
            it.putExtra(USER_ID, userId)
            it.putExtra(CLASS_ID, classId)
        }
        startActivity(intent)
    }

    override fun hideLoading() {
        swipe_refresh?.isRefreshing = false
    }
}