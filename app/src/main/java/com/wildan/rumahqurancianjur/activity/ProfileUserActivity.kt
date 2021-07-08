package com.wildan.rumahqurancianjur.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.utils.UtilsConstant.USER_ID
import kotlinx.android.synthetic.main.activity_profile_user.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ProfileUserActivity : AppCompatActivity() {

    private lateinit var memberUserId: String
    private var mUsername: String? = null
    private var mNomorInduk: String? = null
    private var mEmail: String? = null
    private var mGender: String? = null
    private var mPhone: String? = null
    private var mAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_user)
        init()
        getProfileData()
        getPhotoProfile()

        swipe_refresh.setOnRefreshListener {
            getProfileData()
        }
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        memberUserId = intent?.getStringExtra(USER_ID).toString()
    }

    private fun getProfileData() {
        swipe_refresh.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
            .collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(memberUserId)

        db.addSnapshotListener { it, _ ->
            mNomorInduk = it?.getString("registrationNumber").toString()
            mUsername = it?.getString("username").toString()
            mEmail = it?.getString("email").toString()
            mAddress = it?.getString("address").toString()
            mPhone = it?.getString("phone").toString()
            mGender = it?.getString("gender").toString()

            tv_username?.text = mUsername
            tv_email?.text = mEmail
            tv_address?.text = mAddress
            tv_phone_number?.text = mPhone
            tv_gender?.text = mGender
            tv_nomor_induk?.text = mNomorInduk
        }
    }

    private fun getPhotoProfile() {
        val db = FirebaseFirestore.getInstance()
        db.collection("photos")
            .document(memberUserId)
            .get()
            .addOnSuccessListener {
                swipe_refresh.isRefreshing = false

                val photoUrl = it.getString("photoUrl").toString()
                GlideApp.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(img_profile)
            }
            .addOnFailureListener {
                swipe_refresh.isRefreshing = false
                val message = resources.getString(R.string.request_error)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}