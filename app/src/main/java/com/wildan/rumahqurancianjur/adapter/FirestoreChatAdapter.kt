package com.wildan.rumahqurancianjur.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.model.ChatMessage
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TYPE_MESSAGE_RECEIVED
import kotlinx.android.synthetic.main.message_friend_item.view.*
import kotlinx.android.synthetic.main.message_user_item.view.*
import java.text.SimpleDateFormat

class FirestoreChatAdapter(
    private val options: FirestoreRecyclerOptions<ChatMessage>
) :
    FirestoreRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val view: View

        if (viewType == TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_friend_item, parent, false)
            viewHolder =
                ReceivedMessageViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_user_item, parent, false)
            viewHolder = SentMessageViewHolder(view)
        }

        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        val type: Int
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        type = if (options.snapshots[position].userId.toString() == userId) {
            0
        } else {
            1
        }
        return type
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        item: ChatMessage
    ) {
        val message = getItem(position)
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val view = holder.itemView
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)

        if (message.userId == userId) {

            val messageUser = view.input_user_message
            val dateUserMessage = view.tv_user_date

            messageUser.text = message.text.toString()
            dateUserMessage.text = dataFormat.format(message.date.toString())

        } else {

            val messageFriend = view.input_friend_message
            val dateFriendMessage = view.tv_friend_date

            messageFriend.text = message.text.toString()
            dateFriendMessage.text = dataFormat.format(message.date.toString())

        }
    }

    internal class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}