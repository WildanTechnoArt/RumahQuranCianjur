package com.wildan.rumahqurancianjur.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostData (
    var postType: Int? = null,
    var urlPhoto: String? = null,
    var userId: String? = null,
    var username: String ? = null,
    var nomorInduk: String ? = null,
    var fileUrl: String? = null,
    var postContent: String ? = null
): Parcelable