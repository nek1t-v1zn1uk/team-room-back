package com.example.teamroomback.dtos

data class CreateCourseRequest(
    val name: String,
    val photoUrl: String? = null
)
data class CreateCourseResponse(
    val courseId: Long,
)