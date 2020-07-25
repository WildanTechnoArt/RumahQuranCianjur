package com.wildan.rumahqurancianjur.presenter

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.view.EditProfileView

class EditProfilePresenter(
    private val context: Context,
    private val view: EditProfileView.View
) : EditProfileView.Presenter {

    private val mUserId = SharedPrefManager.getInstance(context).getUserId.toString()
    private var mUsername: String? = null
    private var mEmail: String? = null
    private var mGender: String? = null
    private var mPhone: String? = null
    private var mAddress: String? = null

    override fun showDataUser() {
        val db = FirebaseFirestore.getInstance()
            .collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(mUserId)

        db.get()
            .addOnSuccessListener {
                mUsername = it.getString("username").toString()
                mEmail = it.getString("email").toString()
                mAddress = it.getString("address").toString()
                mPhone = it.getString("phone").toString()
                mGender = it.getString("gender").toString()

                view.hideProgressBar()
                view.showProfileUser(mUsername, mEmail, mPhone, mAddress, mGender)

            }.addOnFailureListener {
                view.hideProgressBar()
                view.handleResponse(it.localizedMessage?.toString().toString())
            }
    }

    override fun requestEditProfile(newEmail: String, newUser: HashMap<String, String>) {
        view.showProgressButton()

        if (mEmail == newEmail) {
            editDataUser(newUser)
        } else {
            val mAuth = FirebaseAuth.getInstance().currentUser
            mAuth?.updateEmail(newEmail)
                ?.addOnCompleteListener {
                    editDataUser(newUser)
                }
                ?.addOnFailureListener {
                    view.handleResponse(it.localizedMessage?.toString().toString())
                    view.hideProgressButton()
                }
        }
    }

    private fun editDataUser(newUser: HashMap<String, String>) {
        val db = FirebaseFirestore.getInstance()
            .collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(mUserId)

        db.set(newUser)
            .addOnSuccessListener {
                view.onSuccessSaveData(context.getString(R.string.success_edit_profile))
            }.addOnFailureListener {
                view.hideProgressButton()
                view.handleResponse(it.localizedMessage?.toString().toString())
            }
    }
}