package com.wildan.rumahqurancianjur.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.ForumMessage
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TYPE_MESSAGE_RECEIVED
import kotlinx.android.synthetic.main.message_friend_item.view.*
import kotlinx.android.synthetic.main.message_user_item.view.*
import java.text.SimpleDateFormat

class FirestoreDiscussionAdapter(
    private val context: Context,
    private val options: FirestoreRecyclerOptions<ForumMessage>
) :
    FirestoreRecyclerAdapter<ForumMessage, RecyclerView.ViewHolder>(options) {

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
        val userId = SharedPrefManager.getInstance(context).getUserId.toString()
        type = if (options.snapshots[position].userId.toString() == userId) {
            0
        } else {
            1
        }
        return type
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        item: ForumMessage
    ) {
        val context = holder.itemView.context
        val message = getItem(position)
        val userId = SharedPrefManager.getInstance(context).getUserId.toString()
        val view = holder.itemView
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)

        if (message.userId == userId) {

            val imgPhotoUser = view.img_user_profile
            val messageUser = view.input_user_message
            val dateUserMessage = view.tv_user_date

            GlideApp.with(context.applicationContext)
                .load(message.photoUrl.toString())
                .placeholder(R.drawable.profile_placeholder)
                .into(imgPhotoUser)

            messageUser.text = message.text.toString()
            dateUserMessage.text = dataFormat.format(message.date)

        } else {

            val imgPhotoFriend = view.img_friend_profile
            val messageFriend = view.input_friend_message
            val dateFriendMessage = view.tv_friend_date
            val username = view.tv_friend_username
            val status = message.status.toString()

            GlideApp.with(context.applicationContext)
                .load(message.photoUrl.toString())
                .placeholder(R.drawable.profile_placeholder)
                .into(imgPhotoFriend)

            username.text = "${message.username.toString()} ($status)"
            messageFriend.text = message.text.toString()
            dateFriendMessage.text = dataFormat.format(message.date)

        }
    }

    internal class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}