package com.wildan.rumahqurancianjur.view

import android.net.Uri
import com.wildan.rumahqurancianjur.model.RegisterResponse

class RegisterView {

    interface View {
        fun onSuccess(userId: String, userEmail: String, message: String)
        fun handleResponse(message: String)
        fun hideProgressBar()
        fun showProgressBar()
    }

    interface Presenter {
        fun requestRegister(
            user: RegisterResponse,
            email: String,
            password: String,
            retype: String,
            result: Uri?
        )
    }
}