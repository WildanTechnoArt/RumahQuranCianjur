package com.wildan.rumahqurancianjur.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.utils.UtilsConstant.LINK_URL
import com.wildan.rumahqurancianjur.utils.UtilsConstant.POST_CONTENT
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TEACHER_NIP
import com.wildan.rumahqurancianjur.utils.UtilsConstant.USERNAME
import kotlinx.android.synthetic.main.fragment_instructions.*

class InstructionsFragment : Fragment() {

    private var mTeacherNip: String? = null
    private var mUsername: String? = null
    private var mPostContent: String? = null
    private var mLinkUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_instructions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAssignmentData()
        setAssignmentData()
    }

    private fun getAssignmentData() {
        mTeacherNip = (context as AppCompatActivity).intent.getStringExtra(TEACHER_NIP)
        mUsername = (context as AppCompatActivity).intent.getStringExtra(USERNAME)
        mPostContent = (context as AppCompatActivity).intent.getStringExtra(POST_CONTENT)
        mLinkUrl = (context as AppCompatActivity).intent.getStringExtra(LINK_URL)
    }

    private fun setAssignmentData() {
        GlideApp.with(this)
            .load(mLinkUrl)
            .placeholder(R.drawable.profile_placeholder)
            .into(img_photo)

        tv_username?.text = mUsername
        tv_teacher_nip?.text = mTeacherNip
        tv_description?.text = mPostContent

        btn_download?.setOnClickListener {
            try {
                val pdfUrl = Uri.parse(mLinkUrl)
                val intent = Intent(Intent.ACTION_VIEW, pdfUrl)
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "Tidak ada aplikasi yang dapat menangani permintaan ini. Silakan instal browser web",
                    Toast.LENGTH_SHORT
                ).show()
                ex.printStackTrace()
            }
        }
    }
}