package com.wildan.rumahqurancianjur.presenter

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.utils.Validation.Companion.validateEmail
import com.wildan.rumahqurancianjur.utils.Validation.Companion.validateFields
import com.wildan.rumahqurancianjur.view.LoginView

class LoginPresenter(
    private val context: Context,
    private val view: LoginView.View
) : LoginView.Presenter {

    override fun requestLogin(email: String, password: String) {
        if (validateFields(email) || validateFields(password)) {
            view.handleResponse(context.getString(R.string.email_password_null))
        } else if (validateEmail(email)) {
            view.handleResponse(context.getString(R.string.email_not_valid))
        } else {
            if (email == "adminrqc@gmail.com") {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_user_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                view.showProgressBar()
                login(email, password)
            }
        }
    }

    private fun login(email: String, password: String) {
        val mAuth = FirebaseAuth.getInstance()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid.toString()
                    val userEmail = task.result?.user?.email.toString()

                    SharedPrefManager.getInstance(context).saveUserId(userId)
                    SharedPrefManager.getInstance(context).saveUserStatus("Guru")
                    SharedPrefManager.getInstance(context).saveUserEmail(userEmail)

                    view.onSuccess()
                } else {
                    view.hideProgressBar()
                    view.handleResponse((task.exception as FirebaseAuthException).errorCode)
                }
            }
    }
}