package com.wildan.rumahqurancianjur.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.activity.GradedActivity
import com.wildan.rumahqurancianjur.model.SubmissionData
import com.wildan.rumahqurancianjur.utils.UtilsConstant.ASSIGNMENT_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.LINK_URL
import com.wildan.rumahqurancianjur.utils.UtilsConstant.STUDENT_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.STUDENT_NAME
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TEXT_ANSWER
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.graded_item.view.*

class TecherGradedAdapter(options: FirestoreRecyclerOptions<SubmissionData>) :
    FirestoreRecyclerAdapter<SubmissionData, TecherGradedAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.graded_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SubmissionData) {

        val context = holder.itemView.context
        val studentId = item.studentId.toString()
        val getTextAnswer = item.textAnswer.toString()
        val getLinkUrl = item.fileUrl.toString()
        val getStudentName = item.username.toString()
        val getStudentId = item.studentId.toString()
        val postId = item.assignmentId.toString()
        val isApproved = item.isApproved

        holder.apply {
            containerView.tv_username.text = item.username.toString()

            val db = FirebaseFirestore.getInstance()
            db.collection("photos")
                .document(studentId)
                .get()
                .addOnSuccessListener {
                    val photoUrl = it.getString("photoUrl").toString()
                    GlideApp.with(context)
                        .load(photoUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_profile)
                }

            if (isApproved == true) {
                containerView.tv_status.text = "Sudah Diperiksa"
                containerView.tv_status.setTextColor(Color.parseColor("#FF48D700"))
            } else {
                containerView.tv_status.text = "Belum Diperiksa"
                containerView.tv_status.setTextColor(Color.parseColor("#FFFFB300"))
            }

            containerView.setOnClickListener {
                val intent = Intent(context, GradedActivity::class.java)
                intent.putExtra(TEXT_ANSWER, getTextAnswer)
                intent.putExtra(STUDENT_NAME, getStudentName)
                intent.putExtra(STUDENT_ID, getStudentId)
                intent.putExtra(ASSIGNMENT_ID, postId)
                intent.putExtra(LINK_URL, getLinkUrl)
                context.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}