package com.wildan.rumahqurancianjur.model

data class RegisterResponse (
    var username: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var gender: String? = null,
    var address: String? = null,
    var teacher: Boolean? = null
)