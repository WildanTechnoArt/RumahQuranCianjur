package com.wildan.rumahqurancianjur.model

data class SubmissionData (
    var textAnswer: String? = null,
    var username: String? = null,
    var assignmentId: String? = null,
    var grade: String? = null,
    var isApproved: Boolean? = null,
    var studentId: String? = null,
    var fileUrl: String? = null
)