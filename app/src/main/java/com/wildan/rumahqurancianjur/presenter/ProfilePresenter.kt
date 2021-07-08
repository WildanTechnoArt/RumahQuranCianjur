package com.wildan.rumahqurancianjur.presenter

import android.content.Context
import android.net.Uri
import com.firebase.ui.auth.AuthUI
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.view.ProfileFragmentView

class ProfilePresenter(
    private val context: Context?,
    private val view: ProfileFragmentView.View
) : ProfileFragmentView.Presenter {

    private val photoReference = FirebaseStorage.getInstance().reference
    private val database = FirebaseFirestore.getInstance()

    private lateinit var listener: ListenerRegistration
    private val mUserId = SharedPrefManager.getInstance(context).getUserId.toString()

    override fun uploadPhotoProfile(imageResult: Uri?) {
        view.showProgressBar()

        val imageURL = "photo_profil/$mUserId"
        val imagePath = photoReference.child(imageURL)

        imageResult?.let {
            imagePath.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    photoReference.child(imageURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->
                        val downloadUrl = imageUri.toString()

                        val dataMap = HashMap<String, String>()
                        dataMap["photoUrl"] = downloadUrl

                        database.collection("photos").document(mUserId)
                            .set(dataMap)
                            .addOnCompleteListener {
                                if (task.isSuccessful) {
                                    view.onSuccessChangePhotoProfile()
                                }
                            }
                    }.addOnFailureListener { it1 ->
                        view.hideProgressBar()
                        view.handleResponse(it1.localizedMessage?.toString().toString())
                    }

                } else {
                    view.hideProgressBar()
                    view.handleResponse(context?.getString(R.string.upload_failed))
                }
            }
        }
    }

    override fun requestLogout() {
        view.showProgressBar()
        context?.let { it ->
            AuthUI.getInstance()
                .signOut(it)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        view.onSuccessLogout()
                    } else {
                        view.hideProgressBar()
                        view.handleResponse(context.getString(R.string.error_request))
                    }
                }
        }
    }

    override fun onDestroy() {
        listener.remove()
    }
}