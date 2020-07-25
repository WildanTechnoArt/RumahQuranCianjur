package com.wildan.rumahqurancianjur.view

class LoginView {

    interface View {
        fun onSuccess()
        fun handleResponse(message: String)
        fun showProgressBar()
        fun hideProgressBar()
    }

    interface Presenter {
        fun requestLogin(email: String, password: String)
    }
}