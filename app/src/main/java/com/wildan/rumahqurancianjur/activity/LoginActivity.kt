package com.wildan.rumahqurancianjur.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.presenter.LoginPresenter
import com.wildan.rumahqurancianjur.view.LoginView
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginView.View {

    private lateinit var presenter: LoginView.Presenter

    private lateinit var mEmail: String
    private lateinit var mPassword: String

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prepare()
        setupListener()
    }

    override fun onSuccess(userId: String) {
        if (checkUserStatus(userId) == true) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            Toast.makeText(
                this, "Pengguna tidak terdaftar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun handleResponse(message: String) {
        when (message) {
            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                this, getString(R.string.error_user_not_found),
                Toast.LENGTH_SHORT
            ).show()

            "ERROR_WRONG_PASSWORD" -> Toast.makeText(
                this, getString(R.string.error_wrong_password),
                Toast.LENGTH_SHORT
            ).show()

            else -> Toast.makeText(
                this, message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun showProgressBar() {
        btn_login.showProgress { progressColor = Color.WHITE }
    }

    override fun hideProgressBar() {
        btn_login.hideProgress(R.string.btn_login)
    }

    private fun prepare() {
        mAnalytics = FirebaseAnalytics.getInstance(this)
        mAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        mAuth = FirebaseAuth.getInstance()

        GlideApp.with(this)
            .load(R.drawable.rqc_green_full)
            .into(img_logo)

        presenter = LoginPresenter(this, this)
        bindProgressButton(btn_login)
        btn_login.attachTextChangeAnimator()
    }

    private fun checkUserStatus(userId: String): Boolean? {
        var status: Boolean? = false
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(userId)
            .get()
            .addOnSuccessListener {
                val getStatus = it.getBoolean("teacher")
                status = getStatus == true
            }.addOnFailureListener {
                btn_login.hideProgress(R.string.btn_login)
                Toast.makeText(
                    this, it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }

        return status
    }

    private fun setupListener() {
        tv_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        tv_forgot_password.setOnClickListener {
            startActivity(Intent(this, ForgotPassActivity::class.java))
        }
        btn_login.setOnClickListener {
            mEmail = input_email.text.toString().trim()
            mPassword = input_password.text.toString().trim()
            presenter.requestLogin(mEmail, mPassword)
        }
    }
}