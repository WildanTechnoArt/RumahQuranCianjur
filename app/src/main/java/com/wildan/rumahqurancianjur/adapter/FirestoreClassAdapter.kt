package com.wildan.rumahqurancianjur.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.interfaces.ClassListListener
import com.wildan.rumahqurancianjur.model.ClassModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.teacher_class_item.view.*

class FirestoreClassAdapter(
    private val context: Context, options: FirestoreRecyclerOptions<ClassModel>,
    private var listener: ClassListListener
) :
    FirestoreRecyclerAdapter<ClassModel, FirestoreClassAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.teacher_class_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ClassModel) {
        val userId = SharedPrefManager.getInstance(context).getUserId.toString()
        val getClassKey = snapshots.getSnapshot(position).id
        val teacherName = item.teacherName.toString()
        val lesson = item.lesson.toString()
        val datetime = item.datetime.toString()
        val level = item.level.toString()

        holder.apply {
            containerView.tv_name.text = teacherName
            containerView.tv_lesson.text = lesson
            containerView.tv_time.text = datetime
            containerView.tv_level.text = level

            // Melihat jumlah pelajaran yang masuk
            val db = FirebaseFirestore.getInstance()
            db.collection("darulfalah")
                .document("classRooms")
                .collection(userId)
                .document(getClassKey)
                .collection("student")
                .get()
                .addOnSuccessListener {
                    val memberCount = it.documents.size
                    containerView.tv_student.text =
                        "${context.getString(R.string.tv_total_student)} $memberCount"
                    listener.hideLoading()
                }
                .addOnFailureListener {
                    listener.hideLoading()
                }

            if (userId == item.teacherUid) {
                containerView.tv_level.setBackgroundResource(R.color.colorPrimary)
                containerView.card_class.setOnClickListener {
                    listener.onClick(userId, getClassKey)
                }
            } else {
                holder.itemView.visibility = View.GONE
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    0,
                    0
                )
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}