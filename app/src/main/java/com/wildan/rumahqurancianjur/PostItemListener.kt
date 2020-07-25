package com.wildan.rumahqurancianjur

interface PostItemListener {
    fun onUpdateClick(teacherUid: String, toolbarName: String, isEdited: Boolean,
                      post: String, postId: String)
    fun onDeletePost(postId: String)
}