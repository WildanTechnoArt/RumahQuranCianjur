package com.wildan.rumahqurancianjur.view

import android.net.Uri

class ProfileFragmentView {

    interface View {
        fun onSuccessLogout()
        fun onSuccessChangePhotoProfile()
        fun handleResponse(message: String?)
        fun hideProgressBar()
        fun showProgressBar()
    }

    interface Presenter {
        fun uploadPhotoProfile(imageResult: Uri?)
        fun requestLogout()
        fun onDestroy()
    }
}