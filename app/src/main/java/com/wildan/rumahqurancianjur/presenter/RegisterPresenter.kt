package com.wildan.rumahqurancianjur.presenter

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.RegisterResponse
import com.wildan.rumahqurancianjur.view.RegisterView

class RegisterPresenter(
    private val context: Context,
    private val view: RegisterView.View
) : RegisterView.Presenter {

    private val photoReference = FirebaseStorage.getInstance().reference
    private val database = FirebaseFirestore.getInstance()

    private var mUserId: String? = null
    private var mEmail: String? = null
    private var mImageResult: Uri? = null
    private lateinit var mUser: RegisterResponse

    override fun requestRegister(
        user: RegisterResponse,
        email: String,
        password: String,
        retype: String,
        result: Uri?
    ) {
        val mAuth = FirebaseAuth.getInstance()
        if (password == retype) {

            view.showProgressBar()

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    mUserId = it.user?.uid.toString()
                    mEmail = it.user?.email.toString()
                    mUser = user
                    mImageResult = result
                    addDataToDatabase()
                }.addOnFailureListener {
                    view.hideProgressBar()
                    view.handleResponse(it.localizedMessage?.toString().toString())
                }
        } else {
            view.handleResponse(context.getString(R.string.password_not_valid))
        }
    }

    private fun addDataToDatabase() {
        val db = FirebaseFirestore.getInstance()
            .collection("darulfalah")
            .document("teacher")
            .collection("newRegistrants")
            .document(mUserId.toString())

        db.set(mUser)
            .addOnSuccessListener {
                uploadPhotoProfile()
            }.addOnFailureListener {
                view.hideProgressBar()
                view.handleResponse(it.localizedMessage?.toString().toString())
            }
    }

    private fun uploadPhotoProfile() {
        view.showProgressBar()

        val imageURL = "photo_profil/$mUserId"
        val imagePath = photoReference.child(imageURL)

        mImageResult?.let {
            imagePath.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    photoReference.child(imageURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                        val downloadUrl = imageUri.toString()

                        val dataMap = HashMap<String, String>()
                        dataMap["photoUrl"] = downloadUrl

                        database.collection("photos").document(mUserId.toString())
                            .set(dataMap)
                            .addOnCompleteListener {
                                if (task.isSuccessful) {
                                    SharedPrefManager.getInstance(context).saveUserPhoto(downloadUrl)
                                    view.onSuccess(
                                        mUserId.toString(),
                                        mEmail.toString(),
                                        context.getString(R.string.register_success)
                                    )
                                }
                            }

                    }.addOnFailureListener { it1 ->
                        view.hideProgressBar()
                        view.handleResponse(it1.localizedMessage?.toString().toString())
                    }

                } else {
                    view.hideProgressBar()
                    view.handleResponse(context.getString(R.string.upload_failed))
                }
            }
        }
    }
}