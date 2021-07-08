package com.wildan.rumahqurancianjur.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.model.PostData
import com.wildan.rumahqurancianjur.view.PostView

class PostPresenter(
    private val context: Context,
    private val view: PostView.View
) : PostView.Presenter {

    private lateinit var db: FirebaseFirestore
    private lateinit var fileUrl: String
    private var resultUri: Uri? = null
    private val fileReference = FirebaseStorage.getInstance().reference

    @SuppressLint("Recycle")
    fun getFileName(uri: Uri?): String {
        var result: String? = null
        if (uri?.scheme.equals("content")) {
            val cursor = uri?.let { context.contentResolver.query(it, null, null, null, null) }
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri?.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    override fun uploadFilePhoto(result: Uri?) {
        resultUri = result
        view.showFileName(getFileName(resultUri))
    }

    override fun uploadFileDocument(result: Uri) {
        resultUri = result
        view.showFileName(getFileName(resultUri))
    }

    override fun requestProfile(userId: String) {
        view.showProgressBar()
        db = FirebaseFirestore.getInstance()
        db.collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(userId)
            .get()
            .addOnSuccessListener {
                val username = it.getString("username").toString()
                val nomorInduk = it.getString("registrationNumber").toString()
                view.showProfileUser(username, nomorInduk)
                view.hideProgressBar()
            }
            .addOnFailureListener {
                it.localizedMessage?.toString()?.let { it1 -> view.handleResponse(it1) }
                view.hideProgressBar()
            }
    }

    override fun addPost(
        userId: String, urlPhoto: String,
        postContent: String?, nomorInduk: String, codeClass: String, username: String
    ) {
        view.showProgressBar()

        if (resultUri != null) {

            val fileURL = "file_tugas/$userId" + "_" + "${resultUri?.lastPathSegment}"
            val filePath = fileReference.child(fileURL)

            filePath.putFile(resultUri!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fileReference.child(fileURL).downloadUrl
                        .addOnSuccessListener { imageUri: Uri? ->
                            fileUrl = imageUri.toString()

                            val data = PostData()
                            data.urlPhoto = urlPhoto
                            data.nomorInduk = nomorInduk
                            data.userId = userId
                            data.username = username
                            data.postContent = postContent
                            data.postType = 0
                            data.fileUrl = fileUrl

                            db = FirebaseFirestore.getInstance()
                            db.collection("classRoom")
                                .document(codeClass)
                                .collection("posts")
                                .document()
                                .set(data)
                                .addOnSuccessListener {
                                    view.onSuccessPost(context.getString(R.string.success_upload_post))
                                }
                                .addOnFailureListener {
                                    view.handleResponse(it.localizedMessage?.toString().toString())
                                    view.hideProgressBar()
                                }

                        }.addOnFailureListener {
                            view.hideProgressBar()
                            view.handleResponse(it.localizedMessage?.toString().toString())
                        }

                } else {
                    view.hideProgressBar()
                    view.handleResponse(context.getString(R.string.upload_failed))
                }
            }

        } else {
            val data = PostData()
            data.urlPhoto = urlPhoto
            data.nomorInduk = nomorInduk
            data.userId = userId
            data.username = username
            data.userId = userId
            data.postType = 1
            data.postContent = postContent

            db = FirebaseFirestore.getInstance()
            db.collection("classRoom")
                .document(codeClass)
                .collection("posts")
                .document()
                .set(data)
                .addOnSuccessListener {
                    view.onSuccessPost(context.getString(R.string.success_upload_post))
                }
                .addOnFailureListener {
                    it.localizedMessage?.toString()?.let { it1 -> view.handleResponse(it1) }
                    view.hideProgressBar()
                }
        }
    }

    override fun updatePost(
        userId: String,
        postKey: String,
        urlPhoto: String,
        postContent: String?,
        nomorInduk: String,
        codeClass: String,
        username: String
    ) {
        view.showProgressBar()

        val data = PostData()
        data.urlPhoto = urlPhoto
        data.nomorInduk = nomorInduk
        data.userId = userId
        data.username = username
        data.postContent = postContent

        db = FirebaseFirestore.getInstance()
        db.collection("classRoom")
            .document(codeClass)
            .collection("posts")
            .document(postKey)
            .set(data)
            .addOnSuccessListener {
                view.onSuccessUpdate(context.getString(R.string.update_success))
            }
            .addOnFailureListener {
                view.handleResponse(it.localizedMessage?.toString().toString())
                view.hideProgressBar()
            }
    }

    override fun requestPhoto(userId: String) {
        view.showProgressBar()
        db = FirebaseFirestore.getInstance()
        db.collection("photos")
            .document(userId)
            .get()
            .addOnSuccessListener {
                val url = it.getString("photoUrl").toString()
                view.showPhotoUser(url)
                view.hideProgressBar()
            }
            .addOnFailureListener {
                it.localizedMessage?.toString()?.let { it1 -> view.handleResponse(it1) }
                view.hideProgressBar()
            }
    }
}