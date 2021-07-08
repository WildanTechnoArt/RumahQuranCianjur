package com.wildan.rumahqurancianjur.view

import android.net.Uri

class PostView {

    interface View {
        fun handleResponse(message: String)
        fun showProfileUser(username: String, nomorInduk: String)
        fun showPhotoUser(photoUrl: String)
        fun onSuccessPost(message: String)
        fun onSuccessUpdate(message: String)
        fun showFileName(fileName: String)
        fun hideProgressBar()
        fun showProgressBar()
    }

    interface Presenter {
        fun requestProfile(userId: String)
        fun addPost(
            userId: String,
            urlPhoto: String,
            postContent: String?,
            nomorInduk: String,
            codeClass: String,
            username: String
        )

        fun updatePost(
            userId: String,
            postKey: String,
            urlPhoto: String,
            postContent: String?,
            nomorInduk: String,
            codeClass: String,
            username: String
        )

        fun requestPhoto(userId: String)

        fun uploadFilePhoto(result: Uri?)
        fun uploadFileDocument(result: Uri)
    }
}