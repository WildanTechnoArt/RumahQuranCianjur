package com.wildan.rumahqurancianjur.presenter

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.utils.Validation.Companion.validateEmail
import com.wildan.rumahqurancianjur.view.ForgotPasswordView

class ForgotPasswordPresenter(
    private val context: Context,
    private val view: ForgotPasswordView.View
) : ForgotPasswordView.Presenter {

    override fun requestForgotPassword(email: String) {
        if (validateEmail(email)) {
            view.handleResponse(context.getString(R.string.email_not_valid))
        } else {
            view.showProgressBar()

            val mAuth = FirebaseAuth.getInstance()

            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        view.onSuccess(context.getString(R.string.send_reset_password))
                        view.hideProgressBar()
                    } else {
                        view.hideProgressBar()
                        view.handleResponse(context.getString(R.string.email_not_valid))
                    }
                }
        }
    }
}