package com.wildan.rumahqurancianjur.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.PostItemListener
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.activity.CreatePostActivity
import com.wildan.rumahqurancianjur.adapter.FirestorePostAdapter
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.PostData
import com.wildan.rumahqurancianjur.utils.UtilsConstant.CLASS_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.GET_POST
import com.wildan.rumahqurancianjur.utils.UtilsConstant.IS_EDITED
import com.wildan.rumahqurancianjur.utils.UtilsConstant.POST_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TOOLBAR_TITLE
import kotlinx.android.synthetic.main.fragment_class_post.*

class ClassPostFragment : Fragment(), PostItemListener {

    private lateinit var userId: String
    private lateinit var codeClass: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_class_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = SharedPrefManager.getInstance(view.context).getUserId.toString()
        codeClass = (context as AppCompatActivity).intent?.getStringExtra(CLASS_ID).toString()
        setupDatabse()
        getDataCount()
        swipe_refresh.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }
    }

    override fun onUpdateClick(
        teacherUid: String,
        toolbarName: String,
        isEdited: Boolean,
        post: String,
        postId: String
    ) {

        val intent = Intent(context, CreatePostActivity::class.java)

        intent.putExtra(TOOLBAR_TITLE, toolbarName)
        intent.putExtra(IS_EDITED, isEdited)
        intent.putExtra(GET_POST, post)
        intent.putExtra(POST_ID, postId)
        intent.putExtra(CLASS_ID, codeClass)
        startActivity(intent)
    }

    override fun onDeletePost(postId: String) {
        swipe_refresh.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
        db.collection("classRoom")
            .document(codeClass)
            .collection("posts")
            .document(postId)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("classRoom")
            .document(codeClass)
            .collection("posts")

        val options = FirestoreRecyclerOptions.Builder<PostData>()
            .setQuery(query, PostData::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_post_list.layoutManager = LinearLayoutManager(context)
        rv_post_list.setHasFixedSize(true)

        val adapter = FirestorePostAdapter(options, this)
        rv_post_list.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("classRoom")
            .document(codeClass)
            .collection("posts")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_post_list?.visibility = View.VISIBLE
                tv_nothing_posts?.visibility = View.GONE
            } else {
                rv_post_list?.visibility = View.GONE
                tv_nothing_posts?.visibility = View.VISIBLE
            }
            swipe_refresh.isRefreshing = false
        }
    }
}