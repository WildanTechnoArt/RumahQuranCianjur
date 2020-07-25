package com.wildan.rumahqurancianjur.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theartofdev.edmodo.cropper.CropImage
import com.wildan.rumahqurancianjur.GlideApp
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.activity.RegisterActivity.Companion.GALLERY_PICK
import com.wildan.rumahqurancianjur.activity.RegisterActivity.Companion.PERMISSION_STORAGE
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.presenter.PostPresenter
import com.wildan.rumahqurancianjur.utils.UtilsConstant.CLASS_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.GET_POST
import com.wildan.rumahqurancianjur.utils.UtilsConstant.IS_EDITED
import com.wildan.rumahqurancianjur.utils.UtilsConstant.POST_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TOOLBAR_TITLE
import com.wildan.rumahqurancianjur.view.PostView
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class CreatePostActivity : AppCompatActivity(), PostView.View {

    private lateinit var presenter: PostView.Presenter
    private lateinit var userId: String
    private lateinit var username: String
    private lateinit var nomorInduk: String
    private lateinit var classId: String
    private lateinit var photoUrl: String
    private var post: String? = null

    private var isDocument: Boolean? = false
    private var isEdited: Boolean? = false
    private lateinit var getPostId: String
    private lateinit var getPostContent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        prepare()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_create_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        post = input_post.text.toString().trim()
        when (item.itemId) {
            R.id.post -> {
                if (post != null) {
                    if (isEdited == true) {
                        getPostId = intent?.getStringExtra(POST_ID).toString()
                        presenter.updatePost(
                            userId,
                            getPostId,
                            photoUrl,
                            post,
                            nomorInduk,
                            classId,
                            username
                        )
                    } else {
                        presenter.addPost(userId, photoUrl, post, nomorInduk, classId, username)
                    }
                } else {
                    Toast.makeText(this, "Isi postingan tidak boleh kosong", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return true
    }

    override fun onSuccessPost(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSuccessUpdate(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun handleResponse(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showProfileUser(username: String, nomorInduk: String) {
        this.username = username
        this.nomorInduk = nomorInduk
        tv_teacher_name.text = username
        tv_teacher_id.text = nomorInduk
    }

    override fun showPhotoUser(photoUrl: String) {
        this.photoUrl = photoUrl
        GlideApp.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.profile_placeholder)
            .into(img_profile)
    }

    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    override fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
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

                    if (isDocument == true) {
                        val galleryIntent = Intent()
                        galleryIntent.type = "file/*"
                        galleryIntent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(galleryIntent, "SELECT FILE"),
                            FILE_PICK
                        )
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
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICK && resultCode == Activity.RESULT_OK) {

            val fileData = data?.data
            fileData?.let { presenter.uploadFileDocument(it) }

        } else if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(200, 200)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {

                presenter.uploadFilePhoto(result)

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(
                    this, "Crop Image Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun showFileName(fileName: String) {
        tv_file_name.text = fileName
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        val titleBar = intent?.getStringExtra(TOOLBAR_TITLE).toString()
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = titleBar
        }

        presenter = PostPresenter(this, this)
        userId = SharedPrefManager.getInstance(this).getUserId.toString()
        classId = intent?.getStringExtra(CLASS_ID).toString()
        isEdited = intent?.getBooleanExtra(IS_EDITED, false)

        presenter.requestProfile(userId)
        presenter.requestPhoto(userId)


        if (isEdited == true) {
            getPostContent = intent?.getStringExtra(GET_POST).toString()
            input_post.setText(getPostContent)
        }

        fab_create_post.setOnClickListener {
            val itemMenu = arrayOf("Upload File Dokumen", "Upload File Gambar")
            val alert = MaterialAlertDialogBuilder(this)
                .setTitle("Pilih Opsi")
                .setItems(itemMenu) { _, item ->
                    when (item) {
                        0 -> {
                            isDocument = true
                            getFileFromStorage()
                        }
                        1 -> {
                            isDocument = false
                            getPhotoFromStorage()
                        }
                    }
                }
            alert.create()
            alert.show()
        }
    }

    private fun getFileFromStorage() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_STORAGE
            )

        } else {

            val mimeTypes = arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.ms-powerpoint",
                "application/vnd.ms-excel",
                "text/plain"
            )

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                if (mimeTypes.isNotEmpty()) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                var mimeTypesStr = ""
                for (mimeType in mimeTypes) {
                    mimeTypesStr += "$mimeType|"
                }
                intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
            }

            startActivityForResult(Intent.createChooser(intent, "SELECT FILE"), FILE_PICK)
        }
    }

    private fun getPhotoFromStorage() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_STORAGE
            )

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
        const val FILE_PICK = 3
    }
}