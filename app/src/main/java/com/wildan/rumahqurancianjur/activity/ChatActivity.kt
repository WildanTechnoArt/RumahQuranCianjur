package com.wildan.rumahqurancianjur.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.adapter.FirestoreChatAdapter
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.ChatMessage
import com.wildan.rumahqurancianjur.model.ListChat
import com.wildan.rumahqurancianjur.utils.UtilsConstant.STUDENT_ID
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_appbar.*
import java.util.*

class ChatActivity : AppCompatActivity() {

    private var adapter: FirestoreChatAdapter? = null
    private lateinit var userId: String
    private lateinit var studentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        prepare()
        getChatMessage()
        checkChatList()

        input_message.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btn_send_message.isEnabled = s.toString().isNotEmpty()
            }

        })

        btn_send_message.setOnClickListener {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(swipe_refresh.windowToken, 0)
            sendChatMessage()
        }
    }

    private fun getChatMessage() {
        val query = FirebaseFirestore.getInstance()
            .collection("chatList")
            .document(studentId)
            .collection(userId)
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ChatMessage>()
            .setQuery(query, ChatMessage::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_chat_list.layoutManager = LinearLayoutManager(this)
        rv_chat_list.setHasFixedSize(true)

        adapter = FirestoreChatAdapter(options)
        rv_chat_list.adapter = adapter
    }

    private fun prepare() {
        swipe_refresh.isEnabled = false
        rv_chat_list.layoutManager = LinearLayoutManager(this)
        rv_chat_list.setHasFixedSize(true)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        studentId = intent?.getStringExtra(STUDENT_ID).toString()

        getPhotoUrl()
        getUsername()
    }

    private fun getPhotoUrl() {
        val db = FirebaseFirestore.getInstance()
        db.collection("photos")
            .document(studentId)
            .get()
            .addOnSuccessListener {
                val photoUrl = it.getString("photoUrl").toString()
                GlideApp.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(tv_photo)
            }
    }

    private fun getUsername() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(studentId)
            .get()
            .addOnSuccessListener {
                val username = it.getString("username").toString()
                tv_username.text = username
            }
    }

    private fun sendChatMessage() {
        swipe_refresh.isRefreshing = true

        val data = ChatMessage()
        data.username = SharedPrefManager.getInstance(this).getUserName.toString()
        data.text = input_message.text.toString()
        data.status = SharedPrefManager.getInstance(this).getUserStatus.toString()
        data.userId = userId
        data.date = Calendar.getInstance().time

        val db = FirebaseFirestore.getInstance()
        db.collection("chatList")
            .document(userId)
            .collection(studentId)
            .document()
            .set(data)
            .addOnSuccessListener {
                sendChatToFriend()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Message failed to send", Toast.LENGTH_SHORT).show()
                swipe_refresh.isRefreshing = false

            }
    }

    private fun sendChatToFriend() {
        swipe_refresh.isRefreshing = true

        val data = ChatMessage()
        data.username = SharedPrefManager.getInstance(this).getUserName.toString()
        data.text = input_message.text.toString()
        data.status = SharedPrefManager.getInstance(this).getUserStatus.toString()
        data.userId = userId
        data.date = Calendar.getInstance().time

        val db = FirebaseFirestore.getInstance()
        db.collection("chatList")
            .document(studentId)
            .collection(userId)
            .document()
            .set(data)
            .addOnSuccessListener {
                input_message.setText("")
                swipe_refresh.isRefreshing = false
                rv_chat_list.post {
                    adapter?.itemCount?.minus(1)
                        ?.let { it1 -> rv_chat_list.smoothScrollToPosition(it1) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Message failed to send", Toast.LENGTH_SHORT).show()
                swipe_refresh.isRefreshing = false

            }
    }

    private fun checkChatList() {
        val db = FirebaseFirestore.getInstance()
        db.collection("chatList")
            .document(studentId)
            .collection(userId)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot?.isEmpty == false) {
                    getStudentData()
                    getTeacherData()
                }
            }
    }

    private fun getStudentData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(studentId)
            .get()
            .addOnSuccessListener {
                swipe_refresh.isRefreshing = false
                val getUsername = it.getString("username").toString()
                val getNisn = it.getString("nisn").toString()
                addStudentToChatList(getUsername, getNisn)
            }
            .addOnFailureListener {
                swipe_refresh.isRefreshing = false
                val message = resources.getString(R.string.request_error)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTeacherData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                swipe_refresh.isRefreshing = false
                val getUsername = it.getString("username").toString()
                val getNip = it.getString("nip").toString()
                addTeacherToChatList(getUsername, getNip)
            }
            .addOnFailureListener {
                swipe_refresh.isRefreshing = false
                val message = resources.getString(R.string.request_error)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun addStudentToChatList(username: String, nisn: String) {
        val data = ListChat()
        data.username = username
        data.userId = studentId
        data.nomorInduk = nisn

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document("chat")
            .collection(userId)
            .document(studentId)
            .set(data)
    }

    private fun addTeacherToChatList(username: String, nip: String) {
        val data = ListChat()
        data.username = username
        data.userId = userId
        data.nomorInduk = nip

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document("chat")
            .collection(studentId)
            .document(userId)
            .set(data)
    }
}