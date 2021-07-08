package com.wildan.rumahqurancianjur.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MemberData (
    var userId: String? = null,
    var username: String ? = null,
    var status: String ? = null
): Parcelable