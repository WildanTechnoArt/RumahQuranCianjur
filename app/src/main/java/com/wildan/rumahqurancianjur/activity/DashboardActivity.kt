package com.wildan.rumahqurancianjur.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.wildan.rumahqurancianjur.R
import com.wildan.rumahqurancianjur.database.SharedPrefManager
import com.wildan.rumahqurancianjur.fragment.ClassListFragment
import com.wildan.rumahqurancianjur.fragment.MessageFragment
import com.wildan.rumahqurancianjur.fragment.ProfileFragment
import com.wildan.rumahqurancianjur.utils.UtilsConstant.KEY_FRAGMENT
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.net.URLEncoder

class DashboardActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var pageContent: Fragment? = ClassListFragment()
    private var mUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        init(savedInstanceState)
        getDataUser()
        btn_request.setOnClickListener {
            val phoneNumber = "+62 85723398918"
            val message = """
                Assalamualaikum.
                Mohon Admin untuk menyetujui saya sebagai Pengajar.
                
                Berikut ini adalah Alamat Email saya:
                ${SharedPrefManager.getInstance(this).getUserEmail.toString()}
                
                Mohon untuk disetujui, Terimakasih.
            """.trimIndent()

            val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                    URLEncoder.encode(message, "UTF-8")
            try {
                val pm: PackageManager = packageManager
                pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(
                    this,
                    getString(R.string.no_installed_message),
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
        btn_logout.setOnClickListener {
            requestLogout()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        pageContent?.let { supportFragmentManager.putFragment(outState, KEY_FRAGMENT, it) }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            R.id.class_room -> pageContent = ClassListFragment()
            R.id.chat -> pageContent = MessageFragment()
            R.id.profile -> pageContent = ProfileFragment()
        }
        pageContent?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.view_pager, it)
                .commit()
        }
        return true
    }

    private fun init(savedInstanceState: Bundle?) {
        mUserId = SharedPrefManager.getInstance(this).getUserId.toString()

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.view_pager, it).commit()
            }
        } else {
            pageContent = supportFragmentManager.getFragment(savedInstanceState, KEY_FRAGMENT)
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.view_pager, it).commit()
            }
        }
    }

    private fun requestLogout() {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi")
            .setMessage("Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                btn_logout.showProgress { Color.WHITE }
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            SharedPrefManager.getInstance(this).deleteUser()
                            finish()
                        } else {
                            btn_logout.hideProgress(getString(R.string.logout))
                            Toast.makeText(
                                this,
                                getString(R.string.error_request),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun getUserStatus() {
        val userId = SharedPrefManager.getInstance(this).getUserId.toString()
        val db = FirebaseFirestore.getInstance()
        db.collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.exists() == true) {
                    isApproved()
                } else {
                    disApproved()
                }
            }
    }

    private fun isApproved() {
        progressBar?.visibility = View.GONE
        btn_request?.visibility = View.GONE
        btn_logout?.visibility = View.GONE
        tv_information?.visibility = View.GONE
        bottom_navigation?.visibility = View.VISIBLE
        view_pager?.visibility = View.VISIBLE
        appbar?.visibility = View.GONE
    }

    private fun disApproved() {
        progressBar?.visibility = View.GONE
        btn_request?.visibility = View.VISIBLE
        btn_logout?.visibility = View.VISIBLE
        tv_information?.visibility = View.VISIBLE
        bottom_navigation?.visibility = View.GONE
        view_pager?.visibility = View.GONE
        appbar?.visibility = View.VISIBLE

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        bindProgressButton(btn_logout)
        btn_logout.attachTextChangeAnimator()
    }

    private fun getDataUser() {
        progressBar?.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
            .collection("darulfalah")
            .document("teacher")
            .collection("teacherList")
            .document(mUserId.toString())
            .get()

        db.addOnSuccessListener {
            val username = it?.getString("username").toString()
            SharedPrefManager.getInstance(this).saveUsername(username)
            getPhotoUrl()
        }.addOnFailureListener {
                progressBar?.visibility = View.GONE
                Toast.makeText(
                    this,
                    it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getPhotoUrl() {
        val db = FirebaseFirestore.getInstance()
            .collection("photos")
            .document(mUserId.toString())

        db.get()
            .addOnSuccessListener {
                val getImageUrl = it.getString("photoUrl").toString()
                SharedPrefManager.getInstance(this).saveUserPhoto(getImageUrl)
                getUserStatus()
            }.addOnFailureListener {
                progressBar?.visibility = View.GONE
                Toast.makeText(
                    this,
                    it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
