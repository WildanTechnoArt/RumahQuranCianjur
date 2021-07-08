package com.wildan.rumahqurancianjur.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.activity.RegisterActivity.Companion.PERMISSION_STORAGE
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.model.SubmissionData
import com.wildan.rumahqurancianjur.utils.UtilsConstant
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_submissions.*
import kotlinx.android.synthetic.main.fragment_submissions.line_three
import kotlinx.android.synthetic.main.fragment_submissions.swipe_refresh

class SubmissionsFragment : Fragment() {

    private val fileReference = FirebaseStorage.getInstance().reference
    private var mTeacherId: String? = null
    private var mAssignmentId: String? = null
    private var mStudentId: String? = null
    private var mStudentName: String? = null
    private var menuItem: Menu? = null
    private var fileUri: Uri? = null
    private var isDocument = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_submissions, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        init(view)
        checkSubmission()
    }

    private fun init(view: View) {
        swipe_refresh?.isEnabled = false
        btn_attach.isEnabled = false

        mTeacherId = (context as AppCompatActivity).intent?.getStringExtra(UtilsConstant.TEACHER_ID)
            .toString()
        mAssignmentId =
            (context as AppCompatActivity).intent?.getStringExtra(UtilsConstant.ASSIGNMENT_ID)
                .toString()
        mStudentId = SharedPrefManager.getInstance(context).getUserId.toString()
        mStudentName = SharedPrefManager.getInstance(context).getUserName.toString()

        btn_attach.setOnClickListener {
            val itemMenu = arrayOf("Upload File Dokumen", "Upload File Gambar")
            val alert = AlertDialog.Builder(view.context)
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

    private fun getPhotoFromStorage() {
        if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED && activity?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_STORAGE
                )
            }

        } else {
            pickImage()
        }
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(1024, 1024)
            .start { resultCode, data ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        fileUri = data?.data
                        tv_file_name.text = getFileName(fileUri)
                    }
                    ImagePicker.RESULT_ERROR -> {
                        progress_bar?.visibility = View.GONE
                        Toast.makeText(
                            context,
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun getFileFromStorage() {
        if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED && activity?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_STORAGE
                )
            }

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

            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.isNotEmpty()) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }

            fileStartForResult.launch(Intent.createChooser(intent, "SELECT FILE"))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        @Suppress("DEPRECATION")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isDocument) {
                        val galleryIntent = Intent()
                        galleryIntent.type = "file/*"
                        galleryIntent.action = Intent.ACTION_GET_CONTENT
                        fileStartForResult.launch(Intent.createChooser(galleryIntent, "SELECT FILE"))
                    } else {
                        pickImage()
                    }
                }
                return
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkSubmission() {
        swipe_refresh?.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
        db.collection("submissions")
            .document(mTeacherId.toString())
            .collection(mAssignmentId.toString())
            .document(mStudentId.toString())
            .addSnapshotListener { snapshot, _ ->
                swipe_refresh?.isRefreshing = false
                btn_attach?.isEnabled = true
                if (snapshot?.exists() == false) {
                    tv_submission_status.setTextColor(Color.parseColor("#FFFFB300"))
                    tv_submission_status.text = "Unverified"
                } else {
                    btn_attach.visibility = View.GONE
                    input_text.isEnabled = false

                    val getTextAnswer = snapshot?.getString("textAnswer")
                    val isApproved = snapshot?.getBoolean("approved")
                    val grade = snapshot?.getString("grade")

                    line.visibility = View.VISIBLE
                    line_three.visibility = View.VISIBLE
                    tv_grade.visibility = View.VISIBLE
                    tv_status.visibility = View.VISIBLE
                    tv_submission_status.visibility = View.VISIBLE

                    if (isApproved == true) {
                        tv_submission_status?.text = "Sudah Diperiksa"
                        tv_submission_status?.setTextColor(Color.parseColor("#FF48D700"))
                    } else {
                        tv_submission_status?.text = "Belum Diperiksa"
                        tv_submission_status?.setTextColor(Color.parseColor("#FFFFB300"))
                    }

                    input_text.setText(getTextAnswer.toString())
                    tv_grade.text = "Grade: $grade/100"
                }
            }
    }

    private val fileStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                fileUri = result.data?.data
                tv_file_name.text = getFileName(fileUri)
            }
        }

    private fun uploadFile() {
        val db = FirebaseFirestore.getInstance()

        if (fileUri != null) {
            swipe_refresh?.isRefreshing = true

            val fileURL = "submissions/$mStudentId" + "_" + "${fileUri?.lastPathSegment}"
            val filePath = fileReference.child(fileURL)

            filePath.putFile(fileUri!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fileReference.child(fileURL).downloadUrl
                        .addOnSuccessListener { imageUri: Uri? ->
                            val fileUrl = imageUri.toString()
                            val data = SubmissionData()
                            data.textAnswer = input_text.text.toString()
                            data.fileUrl = fileUrl
                            data.studentId = mStudentId.toString()
                            data.isApproved = false
                            data.assignmentId = mAssignmentId.toString()
                            data.grade = "0"
                            data.username = mStudentName

                            db.collection("submissions")
                                .document(mTeacherId.toString())
                                .collection(mAssignmentId.toString())
                                .document(mStudentId.toString())
                                .set(data)
                                .addOnSuccessListener {
                                    swipe_refresh?.isRefreshing = false
                                    input_text.isEnabled = false
                                    tv_file_name.visibility = View.GONE
                                    btn_attach.visibility = View.GONE
                                    line.visibility = View.VISIBLE
                                    line_three.visibility = View.VISIBLE
                                    tv_grade.visibility = View.VISIBLE
                                    tv_status.visibility = View.VISIBLE
                                    tv_submission_status.visibility = View.VISIBLE
                                    Toast.makeText(
                                        context,
                                        "Task Submitted Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    checkSubmission()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        context?.getString(R.string.error_request),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    swipe_refresh?.isRefreshing = false
                                }

                        }.addOnFailureListener {
                            swipe_refresh?.isRefreshing = false
                            Toast.makeText(
                                context,
                                context?.getString(R.string.upload_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {
                    swipe_refresh?.isRefreshing = false
                    Toast.makeText(
                        context,
                        context?.getString(R.string.upload_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(context, "Tidak ada file yang di upload", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri?): String {
        var result: String? = null
        if (uri?.scheme.equals("content")) {
            val cursor = uri?.let { context?.contentResolver?.query(it, null, null, null, null) }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuItem = menu
        var item: MenuItem?

        val db = FirebaseFirestore.getInstance()
        db.collection("submissions")
            .document(mTeacherId.toString())
            .collection(mAssignmentId.toString())
            .document(mStudentId.toString())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.exists() == true) {
                    val getIsApproved = snapshot.getBoolean("approved")
                    if (getIsApproved == true) {
                        item = menuItem?.findItem(R.id.submit) as MenuItem
                        item?.title = "KIRIM ULANG"
                        item?.isVisible = true
                    } else {
                        item = menuItem?.findItem(R.id.submit) as MenuItem
                        item?.isVisible = false
                    }
                } else {
                    item = menuItem?.findItem(R.id.submit) as MenuItem
                    item?.title = "KIRIM"
                    item?.isVisible = true
                }
            }
        inflater.inflate(R.menu.student_assignment, menuItem)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.submit -> {
                if (menuItem?.getItem(0)?.title == "KIRIM") {
                    uploadFile()
                } else {
                    menuItem?.getItem(0)?.title = "KIRIM"
                    input_text?.isEnabled = true
                    tv_file_name?.visibility = View.VISIBLE
                    btn_attach?.visibility = View.VISIBLE
                    line?.visibility = View.GONE
                    line_three?.visibility = View.GONE
                    tv_grade?.visibility = View.GONE
                    tv_status?.visibility = View.GONE
                    tv_submission_status?.visibility = View.GONE
                }
            }
        }
        return true
    }
}