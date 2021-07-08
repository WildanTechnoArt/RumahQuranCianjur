package com.wildan.rumahqurancianjur.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.presenter.EditProfilePresenter
import com.wildan.rumahqurancianjur.utils.UtilsConstant.NOMOR_INDUK
import com.wildan.rumahqurancianjur.view.EditProfileView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditProfileActivity : AppCompatActivity(), EditProfileView.View {

    private lateinit var newUsername: String
    private lateinit var mNomorInduk: String
    private lateinit var newEmail: String
    private lateinit var newPhoneNumber: String
    private var newGender: String = ""
    private lateinit var newAddress: String

    private lateinit var presenter: EditProfileView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        prepare()

        presenter.showDataUser()

        btn_edit_profile.setOnClickListener {
            newUsername = input_name.text.toString()
            newEmail = input_email.text.toString()
            newPhoneNumber = input_phone.text.toString()
            newAddress = input_address.text.toString()

            val newUser = hashMapOf(
                "registrationNumber" to mNomorInduk,
                "username" to newUsername,
                "email" to newEmail,
                "phone" to newPhoneNumber,
                "gender" to newGender,
                "address" to newAddress
            )

            presenter.requestEditProfile(newEmail, newUser)
        }

        rg_gender.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_male -> newGender = "Laki-laki"
                R.id.rb_famale -> newGender = "Perempuan"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onSuccessSaveData(message: String) {
        SharedPrefManager.getInstance(this).saveUsername(newUsername)
        SharedPrefManager.getInstance(this).saveUserEmail(newEmail)
        Toast.makeText(
            this, message,
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun showProfileUser(
        name: String?,
        email: String?,
        phone: String?,
        address: String?,
        gender: String?
    ) {
        input_name.setText(name)
        input_email.setText(email)
        input_phone.setText(phone)
        input_address.setText(address)

        if (gender == "Laki-laki") {
            rb_male.isChecked = true
        } else {
            rb_famale.isChecked = true
        }
    }

    override fun handleResponse(message: String) {
        btn_edit_profile.hideProgress(R.string.btn_edit_profile)
        progressBar.visibility = View.GONE
        Toast.makeText(
            this, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    override fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgressButton() {
        btn_edit_profile.hideProgress(R.string.btn_edit_profile)
    }

    override fun showProgressButton() {
        btn_edit_profile.showProgress { progressColor = Color.WHITE }
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Edit Password"
        }

        mNomorInduk = intent?.getStringExtra(NOMOR_INDUK).toString()

        presenter = EditProfilePresenter(this, this)

        bindProgressButton(btn_edit_profile)
        btn_edit_profile.attachTextChangeAnimator()
    }
}