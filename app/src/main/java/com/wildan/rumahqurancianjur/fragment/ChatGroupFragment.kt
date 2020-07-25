package com.wildan.rumahqurancianjur.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.adapter.FirestoreDiscussionAdapter
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.ForumMessage
import com.wildan.rumahqurancianjur.utils.UtilsConstant.CLASS_ID
import kotlinx.android.synthetic.main.fragment_chat_group.*
import java.util.*

class ChatGroupFragment : Fragment() {

    private var adapter: FirestoreDiscussionAdapter? = null

    private lateinit var userId: String
    private lateinit var codeClass: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        getChatMessage()

        input_message.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btn_send_message.isEnabled = s.toString().isNotEmpty()
            }

        })

        btn_send_message.setOnClickListener {
            val imm: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(swipe_refresh?.windowToken, 0)
            sendChatMessage()
        }
    }

    private fun getChatMessage() {
        val query = FirebaseFirestore.getInstance()
            .collection("classList")
            .document(codeClass)
            .collection("chatRoom")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ForumMessage>()
            .setQuery(query, ForumMessage::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_chat_list.layoutManager = LinearLayoutManager(context)
        rv_chat_list.setHasFixedSize(true)

        adapter = context?.let { FirestoreDiscussionAdapter(it, options) }
        rv_chat_list.adapter = adapter
    }

    private fun prepare(view: View) {
        swipe_refresh?.isEnabled = false
        userId = SharedPrefManager.getInstance(view.context).getUserId.toString()
        codeClass = (context as AppCompatActivity).intent?.getStringExtra(CLASS_ID).toString()
    }

    private fun sendChatMessage() {
        swipe_refresh?.isRefreshing = true

        val data = ForumMessage()
        data.username = SharedPrefManager.getInstance(context).getUserName.toString()
        data.photoUrl = SharedPrefManager.getInstance(context).getUserPhoto.toString()
        data.text = input_message.text.toString()
        data.status = SharedPrefManager.getInstance(context).getUserStatus.toString()
        data.userId = userId
        data.date = Calendar.getInstance().time

        val db = FirebaseFirestore.getInstance()
        db.collection("classList")
            .document(codeClass)
            .collection("chatRoom")
            .document()
            .set(data)
            .addOnSuccessListener {
                input_message.setText("")
                swipe_refresh?.isRefreshing = false
                rv_chat_list.post {
                    adapter?.itemCount?.minus(1)
                        ?.let { it1 -> rv_chat_list.smoothScrollToPosition(it1) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
                swipe_refresh?.isRefreshing = false

            }
    }
}
