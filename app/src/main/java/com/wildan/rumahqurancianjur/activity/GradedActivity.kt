package com.wildan.rumahqurancianjur.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.model.SubmissionData
import com.wildan.rumahqurancianjur.utils.UtilsConstant.ASSIGNMENT_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.LINK_URL
import com.wildan.rumahqurancianjur.utils.UtilsConstant.STUDENT_ID
import com.wildan.rumahqurancianjur.utils.UtilsConstant.STUDENT_NAME
import com.wildan.rumahqurancianjur.utils.UtilsConstant.TEXT_ANSWER
import kotlinx.android.synthetic.main.activity_graded.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class GradedActivity : AppCompatActivity() {

    private lateinit var getTextAnswer: String
    private lateinit var getFileUrl: String
    private lateinit var getStudentName: String
    private lateinit var getStudentId: String
    private lateinit var getTeacherId: String
    private lateinit var getAssignmentId: String
    private var optionMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graded)
        init()
        getSubmission()
        checkSubmission()
        btn_download.setOnClickListener {
            try {
                val pdfUrl = Uri.parse(getFileUrl)
                val intent = Intent(Intent.ACTION_VIEW, pdfUrl)
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Tidak ada aplikasi yang dapat menangani permintaan ini. Silakan instal browser web",
                    Toast.LENGTH_SHORT
                ).show()
                ex.printStackTrace()
            }
        }
    }

    private fun init() {
        setSupportActionBar(toolbar)
        getStudentName = intent?.getStringExtra(STUDENT_NAME).toString()
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = getStudentName
        }
        swipe_refresh.isEnabled = false
    }

    private fun getSubmission() {
        getTextAnswer = intent?.getStringExtra(TEXT_ANSWER).toString()
        getFileUrl = intent?.getStringExtra(LINK_URL).toString()
        getStudentId = intent?.getStringExtra(STUDENT_ID).toString()
        getAssignmentId = intent?.getStringExtra(ASSIGNMENT_ID).toString()
        getTeacherId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        tv_submission.text = getTextAnswer
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        optionMenu = menu
        var item: MenuItem

        val db = FirebaseFirestore.getInstance()
        db.collection("submissions")
            .document(getTeacherId)
            .collection(getAssignmentId)
            .document(getStudentId)
            .addSnapshotListener { snapshot, _ ->
                val getIsApproved = snapshot?.getBoolean("approved")
                if (getIsApproved == true) {
                    item = optionMenu?.findItem(R.id.grade) as MenuItem
                    item.title = "EDIT NILAI"
                    item.isVisible = true
                } else {
                    item = optionMenu?.findItem(R.id.grade) as MenuItem
                    item.title = "BERI NILAI"
                    item.isVisible = true
                }

            }
        menuInflater.inflate(R.menu.grade_activity, optionMenu)
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun checkSubmission() {
        swipe_refresh.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
        db.collection("submissions")
            .document(getTeacherId)
            .collection(getAssignmentId)
            .document(getStudentId)
            .addSnapshotListener { snapshot, _ ->
                swipe_refresh.isRefreshing = false

                val getTextAnswer = snapshot?.getString("textAnswer")
                val isApproved = snapshot?.getBoolean("approved")
                val grade = snapshot?.getString("grade")

                if (isApproved == true) {
                    input_grade.isEnabled = false
                    input_grade.setText("Grade: $grade/100")
                    tv_submission.text = getTextAnswer
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.grade -> {
                if (optionMenu?.getItem(0)?.title == "GRADE") {
                    sendGrade()
                } else {
                    optionMenu?.getItem(0)?.title = "GRADE"
                    input_grade.isEnabled = true
                    input_grade.setText("")
                }
            }
        }
        return true
    }

    private fun sendGrade() {
        swipe_refresh.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        val data = SubmissionData()
        data.textAnswer = getTextAnswer
        data.fileUrl = getFileUrl
        data.studentId = getStudentId
        data.assignmentId = getAssignmentId
        data.isApproved = true
        data.grade = input_grade.text.toString()
        data.username = getStudentName

        db.collection("submissions")
            .document(getTeacherId)
            .collection(getAssignmentId)
            .document(getStudentId)
            .set(data)
            .addOnSuccessListener {
                swipe_refresh.isRefreshing = false
                Toast.makeText(
                    this,
                    "Penilaian Berhasil",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.error_request),
                    Toast.LENGTH_SHORT
                ).show()
                swipe_refresh.isRefreshing = false
            }
    }
}