package com.wildan.rumahqurancianjur.adapter

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.PostItemListener
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.PostData
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TYPE_POST_ASSIGNMENT
import kotlinx.android.synthetic.main.assignment_item.view.*
import kotlinx.android.synthetic.main.post_item.view.*

class FirestorePostAdapter(
    private val options: FirestoreRecyclerOptions<PostData>,
    private val listener: PostItemListener
) :
    FirestoreRecyclerAdapter<PostData, RecyclerView.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val viewHolder: RecyclerView.ViewHolder
        val view: View

        if (viewType == TYPE_POST_ASSIGNMENT) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_item, parent, false)
            viewHolder = PostAssignmentViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item, parent, false)
            viewHolder = PostContentViewHolder(view)
        }

        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return if (options.snapshots[position].postType == TYPE_POST_ASSIGNMENT) {
            0
        } else {
            1
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: PostData) {

        val postId = snapshots.getSnapshot(position).id
        val context = holder.itemView.context
        val post = getItem(position)
        val userId = SharedPrefManager.getInstance(context).getUserId.toString()

        if (post.postType == TYPE_POST_ASSIGNMENT) {
            val view = holder.itemView
            val getUsername = item.username.toString()
            val getPostContent = item.postContent.toString()
            val getTeacherNip = "NIP: ${item.nomorInduk.toString()}"
            val getLinkUrl = item.fileUrl.toString()

            view.tv_assig_username.text = getUsername
            view.tv_assignment_desc.text = getPostContent
            view.tv_assig_id.text = getTeacherNip

            GlideApp.with(context.applicationContext)
                .load(item.urlPhoto.toString())
                .placeholder(R.drawable.profile_placeholder)
                .into(view.img_assig_profile)

            if (post.userId == userId) {
                view.btn_assig_menu.visibility = View.VISIBLE
            } else {
                view.btn_assig_menu.visibility = View.GONE
            }

            view.btn_assig_menu.setOnClickListener {
                val menuItem = arrayOf("Edit", "Hapus")

                val alert = MaterialAlertDialogBuilder(context)
                    .setTitle("Pilih Tindakan")
                    .setItems(menuItem) { _, menu ->
                        when (menu) {
                            0 -> {
                                listener.onUpdateClick(
                                    userId,
                                    "Edit Postingan",
                                    true,
                                    item.postContent.toString(),
                                    postId
                                )
                            }
                            1 -> {
                                listener.onDeletePost(postId)
                            }
                        }
                    }

                alert.create()
                alert.show()
            }

            view.btn_view_assignment.setOnClickListener {
                try {
                    val pdfUrl = Uri.parse(getLinkUrl)
                    val intent = Intent(Intent.ACTION_VIEW, pdfUrl)
                    context.startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "Tidak ada aplikasi yang dapat menangani permintaan ini. Silakan instal browser web",
                        Toast.LENGTH_SHORT
                    ).show()
                    ex.printStackTrace()
                }
            }

        } else {
            val view = holder.itemView

            GlideApp.with(context.applicationContext)
                .load(item.urlPhoto.toString())
                .placeholder(R.drawable.profile_placeholder)
                .into(view.img_profile)

            view.tv_username.text = item.username.toString()
            view.tv_nomor_induk.text = "NIP: ${item.nomorInduk.toString()}"
            view.tv_post_caption.text = item.postContent.toString()

            if (post.userId == userId) {
                view.btn_menu.visibility = View.VISIBLE
            } else {
                view.btn_menu.visibility = View.GONE
            }

            view.btn_menu.setOnClickListener {
                val menuItem = arrayOf("Edit", "Hapus")

                val alert = MaterialAlertDialogBuilder(context)
                    .setTitle("Pilih Tindakan")
                    .setItems(menuItem) { _, menu ->
                        when (menu) {
                            0 -> {
                                listener.onUpdateClick(
                                    userId,
                                    "Edit Postingan",
                                    true,
                                    item.postContent.toString(),
                                    postId
                                )
                            }
                            1 -> {
                                listener.onDeletePost(postId)
                            }
                        }
                    }

                alert.create()
                alert.show()
            }
        }
    }

    inner class PostContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class PostAssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}