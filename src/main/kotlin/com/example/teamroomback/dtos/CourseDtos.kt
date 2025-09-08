package com.example.teamroomback.dtos

import com.example.teamroomback.entities.CourseMemberRole

data class CreateCourseRequest(
    val name: String,
    val photoUrl: String? = null
)
data class CreateCourseResponse(
    val courseId: Long,
)

data class AddCourseMemberRequest(
    val username: String,
    val role: CourseMemberRole
)
data class AddCourseMemberResponse(
    val username: String,
    val courseId: Long,
    val message: String
)