package com.wildan.rumahqurancianjur.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.theartofdev.edmodo.cropper.CropImage
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.presenter.RegisterPresenter
import com.wildan.rumahqurancianjur.utils.Validation
import com.wildan.rumahqurancianjur.view.RegisterView
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class RegisterActivity : AppCompatActivity(), RegisterView.View {

    private lateinit var mUsername: String
    private lateinit var mEmail: String
    private lateinit var mPhoneNumber: String
    private lateinit var mPassword: String
    private lateinit var mReTypePassword: String
    private var mGender: String = ""
    private lateinit var mAddress: String
    private var mImageUri: Uri? = null

    private lateinit var mPresenter: RegisterView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        prepare()
        btn_register.setOnClickListener {
            getInputData()

            val user = hashMapOf(
                "username" to mUsername,
                "email" to mEmail,
                "phone" to mPhoneNumber,
                "gender" to mGender,
                "address" to mAddress
            )

            if (Validation.validateFields(mUsername) || Validation.validateFields(mPassword) ||
                Validation.validateFields(mGender) || Validation.validateFields(mAddress)
            ) {
                Toast.makeText(
                    this, getString(R.string.warning_input_data),
                    Toast.LENGTH_LONG
                ).show()
            } else if (Validation.validateEmail(mEmail)) {
                Toast.makeText(
                    this, getString(R.string.email_not_valid),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (mImageUri == null) {
                    Toast.makeText(
                        this, getString(R.string.not_photo),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (mPassword.length < 6) {
                        Toast.makeText(
                            this,
                            "Masukan Password Minimal 6 Karakter",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        mPresenter.requestRegister(user, mEmail, mPassword, mReTypePassword, mImageUri)
                    }
                }
            }

        }

        rg_gender.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_male -> mGender = "Laki-laki"
                R.id.rb_famale -> mGender = "Perempuan"
            }
        }

        fab_add_photo.setOnClickListener {
            getPhotoFromStorage()
        }
    }

    override fun onSuccess(userId: String, userEmail: String, message: String) {
        SharedPrefManager.getInstance(this).saveUsername(mUsername)
        SharedPrefManager.getInstance(this).saveUserStatus("Guru")
        SharedPrefManager.getInstance(this).saveUserId(userId)
        SharedPrefManager.getInstance(this).saveUserEmail(userEmail)
        startActivity(Intent(this, DashboardActivity::class.java))
        finishAffinity()
    }

    override fun handleResponse(message: String) {
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun hideProgressBar() {
        btn_register.hideProgress(R.string.register)
    }

    override fun showProgressBar() {
        btn_register.showProgress { progressColor = Color.WHITE }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent()
                    galleryIntent.type = "image/*"
                    galleryIntent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                        GALLERY_PICK
                    )
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            val imageUri = data?.data
            CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(200, 200)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {

                mImageUri = result.uri
                GlideApp.with(this)
                    .load(mImageUri)
                    .into(circle_image)

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(
                    this, "Crop Image Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Daftar"
        }

        mPresenter = RegisterPresenter(this, this)

        bindProgressButton(btn_register)
        btn_register.attachTextChangeAnimator()
    }

    private fun getInputData() {
        mUsername = input_name.text.toString()
        mEmail = input_email.text.toString()
        mPhoneNumber = input_phone.text.toString()
        mPassword = input_password.text.toString().trim()
        mReTypePassword = input_retype_password.text.toString().trim()
        mAddress = input_address.text.toString()
    }

    private fun getPhotoFromStorage() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            this.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_STORAGE
                )
            }

        } else {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                GALLERY_PICK
            )
        }
    }

    companion object {
        const val PERMISSION_STORAGE = 1
        const val GALLERY_PICK = 2
    }
}