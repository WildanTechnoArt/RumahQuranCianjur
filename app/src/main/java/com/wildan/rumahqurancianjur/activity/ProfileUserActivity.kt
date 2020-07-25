package com.wildan.rumahqurancianjur.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wildan.rumahqurancianjur.R
import kotlinx.android.synthetic.main.toolbar_layout.*

class ProfileUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_user)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Profi Santri"
    }
}