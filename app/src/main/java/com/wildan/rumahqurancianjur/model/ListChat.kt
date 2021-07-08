package com.wildan.rumahqurancianjur.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListChat(
    var urlPhoto: String? = null,
    var userId: String? = null,
    var username: String? = null,
    var nomorInduk: String? = null
) : Parcelable