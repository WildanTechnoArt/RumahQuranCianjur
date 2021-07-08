package com.wildan.rumahqurancianjur.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.adapter.TecherGradedAdapter
import com.wildan.rumahqurancianjur.model.SubmissionData
import com.wildan.rumahqurancianjur.utils.UtilsConstant
import com.wildan.rumahqurancianjur.utils.UtilsConstant.LINK_URL
import com.wildan.rumahqurancianjur.utils.UtilsConstant.POST_CONTENT
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TEACHER_NIP
import com.wildan.rumahqurancianjur.utils.UtilsConstant.USERNAME
import kotlinx.android.synthetic.main.activity_view_assignment.*
import kotlinx.android.synthetic.main.activity_view_assignment.tv_username
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ViewAssignmentActivity : AppCompatActivity() {

    private var mTeacherNip: String? = null
    private var mUsername: String? = null
    private var mPostContent: String? = null
    private var mLinkUrl: String? = null
    private var mTeacherId: String? = null
    private var mAssignmentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_assignment)
        init()
        getAssignmentData()
        setAssignmentData()
        setupDatabse()
        getDataCount()
        showPhotoProfile()
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Detail Tugas"
        }

        mTeacherId = intent?.getStringExtra(UtilsConstant.TEACHER_ID).toString()
        mAssignmentId = intent?.getStringExtra(UtilsConstant.ASSIGNMENT_ID).toString()

        rv_graded_list?.layoutManager = LinearLayoutManager(this)
        rv_graded_list?.setHasFixedSize(true)
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("submissions")
            .document(mTeacherId.toString())
            .collection(mAssignmentId.toString())

        val options = FirestoreRecyclerOptions.Builder<SubmissionData>()
            .setQuery(query, SubmissionData::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = TecherGradedAdapter(options)
        rv_graded_list?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("submissions")
            .document(mTeacherId.toString())
            .collection(mAssignmentId.toString())

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_graded_list?.visibility = View.VISIBLE
                tv_no_graded?.visibility = View.GONE
            } else {
                rv_graded_list?.visibility = View.GONE
                tv_no_graded?.visibility = View.VISIBLE
            }
        }
    }

    private fun getAssignmentData() {
        mTeacherNip = intent.getStringExtra(TEACHER_NIP)
        mUsername = intent.getStringExtra(USERNAME)
        mPostContent = intent.getStringExtra(POST_CONTENT)
        mLinkUrl = intent.getStringExtra(LINK_URL)
    }

    private fun setAssignmentData() {
        tv_username?.text = mUsername
        tv_teacher_nip?.text = mTeacherNip
        tv_description?.text = mPostContent

        btn_download.setOnClickListener {
            try {
                val fileUrl = Uri.parse(mLinkUrl)
                val intent = Intent(Intent.ACTION_VIEW, fileUrl)
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Tidak ada aplikasi yang dapat menangani permintaan ini. Silakan instal browser web",
                    Toast.LENGTH_SHORT
                ).show()
                ex.printStackTrace()
            }
        }
    }

    private fun showPhotoProfile() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("photos")
            .document(mTeacherId.toString())

        db.get()
            .addOnSuccessListener {
                val getImageUrl = it.getString("photoUrl").toString()
                GlideApp.with(this)
                    .load(getImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(img_photo)

            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}