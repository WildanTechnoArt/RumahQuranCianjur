package com.wildan.rumahqurancianjur.database

import android.content.Context

class SharedPrefManager private constructor(context: Context) {

    init {
        mContext = context
    }

    companion object {

        private const val PROFILE = "profile"

        private const val USER_ID = "userId"
        private const val USER_EMAIL = "userEmail"
        private const val USER_NAME = "userName"
        private const val USER_PHOTO = "userPhoto"
        private const val USER_STATUS = "userStatus"

        private lateinit var mContext: Context
        private var mInstance: SharedPrefManager? = null

        @Synchronized
        fun getInstance(context: Context?): SharedPrefManager {
            if (mInstance == null)
                mInstance = context?.let { SharedPrefManager(it) }
            return mInstance!!
        }
    }

    val getUserId: String?
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(USER_ID, null)
        }

    val getUserName: String?
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(USER_NAME, null)
        }

    val getUserPhoto: String?
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(USER_PHOTO, null)
        }

    val getUserStatus: String?
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(USER_STATUS, null)
        }

    val getUserEmail: String?
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(USER_EMAIL, null)
        }

    fun deleteUser(): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        return editor.commit()
    }

    fun saveUserId(userId: String): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
        return true
    }

    fun saveUsername(username: String): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_NAME, username)
        editor.apply()
        return true
    }

    fun saveUserPhoto(photo: String): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_PHOTO, photo)
        editor.apply()
        return true
    }

    fun saveUserStatus(status: String): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_STATUS, status)
        editor.apply()
        return true
    }

    fun saveUserEmail(userEmail: String): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_EMAIL, userEmail)
        editor.apply()
        return true
    }
}