package com.wildan.rumahqurancianjur.fragment

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.theartofdev.edmodo.cropper.CropImage
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.activity.EditProfileActivity
import com.wildan.rumahqurancianjur.activity.LoginActivity
import com.wildan.rumahqurancianjur.activity.RegisterActivity
import com.wildan.rumahqurancianjur.activity.RegisterActivity.Companion.GALLERY_PICK
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.presenter.ProfilePresenter
import com.wildan.rumahqurancianjur.utils.UtilsConstant.NOMOR_INDUK
import com.wildan.rumahqurancianjur.view.ProfileFragmentView
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ProfileFragment : Fragment(), ProfileFragmentView.View {

    private val mUserId = SharedPrefManager.getInstance(context).getUserId.toString()
    private var mUsername: String? = null
    private var mNomorInduk: String? = null
    private var mEmail: String? = null
    private var mGender: String? = null
    private var mPhone: String? = null
    private var mAddress: String? = null

    private lateinit var presenter: ProfileFragmentView.Presenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        (view.context as AppCompatActivity).setSupportActionBar(toolbar)
        (view.context as AppCompatActivity).supportActionBar?.title = "Profil"

        presenter = ProfilePresenter(view.context, this)

        showUserProfile()
        showPhotoProfile()
        swipe_refresh.setOnRefreshListener {
            showUserProfile()
            showPhotoProfile()
        }

        fab_add_photo.setOnClickListener {
            getPhotoFromStorage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            val imageUri = data?.data
            context?.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it, this)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {
                progress_bar?.visibility = View.VISIBLE
                presenter.uploadPhotoProfile(result.uri)

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                progress_bar?.visibility = View.GONE
                Toast.makeText(
                    context, "Crop Image Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_profile -> {
                val intent = Intent(context, EditProfileActivity::class.java)
                intent.putExtra(NOMOR_INDUK, mNomorInduk)
                startActivity(intent)
            }
            R.id.logout -> {
                val builder = context?.let { MaterialAlertDialogBuilder(it) }
                    ?.setTitle("Konfirmasi")
                    ?.setMessage("Anda yakin ingin keluar?")
                    ?.setPositiveButton("Ya") { _, _ ->
                        presenter.requestLogout()
                    }
                    ?.setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder?.create()
                alert?.show()
            }
        }
        return true
    }

    private fun showUserProfile() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(mUserId)

        db.addSnapshotListener { it, _ ->
            mNomorInduk = it?.getString("registrationNumber").toString()
            mUsername = it?.getString("username").toString()
            mEmail = it?.getString("email").toString()
            mAddress = it?.getString("address").toString()
            mPhone = it?.getString("phone").toString()
            mGender = it?.getString("gender").toString()

            tv_username?.text = mUsername
            tv_email?.text = mEmail
            tv_address?.text = mAddress
            tv_phone_number?.text = mPhone
            tv_gender?.text = mGender
            tv_nomor_induk?.text = mNomorInduk
        }
    }

    private fun showPhotoProfile() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("photos")
            .document(mUserId)

        db.get()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
                val getImageUrl = it.getString("photoUrl").toString()
                SharedPrefManager.getInstance(context).saveUserPhoto(getImageUrl)

                context?.let { it1 ->
                    GlideApp.with(it1)
                        .load(getImageUrl)
                        .into(img_profile)
                }
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(
                    context,
                    it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getPhotoFromStorage() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            this.let {
                ActivityCompat.requestPermissions(
                    context as Activity, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    RegisterActivity.PERMISSION_STORAGE
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

    override fun onSuccessLogout() {
        SharedPrefManager.getInstance(context).deleteUser()
        startActivity(Intent(context, LoginActivity::class.java))
        (context as AppCompatActivity).finish()
    }

    override fun onSuccessChangePhotoProfile() {
        Toast.makeText(context, getString(R.string.change_photo), Toast.LENGTH_SHORT).show()
        progress_bar?.visibility = View.GONE
        showPhotoProfile()
    }

    override fun handleResponse(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun hideProgressBar() {
        swipe_refresh?.isRefreshing = false
    }

    override fun showProgressBar() {
        swipe_refresh?.isRefreshing = true
    }
}