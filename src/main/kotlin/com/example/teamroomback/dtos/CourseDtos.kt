package com.example.teamroomback.dtos

import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMemberRole
import java.time.LocalDateTime

data class CourseDTO(
    val id: Long,
    val name: String,
    val photoUrl: String? = null,
    val isOpen: Boolean,
    val members: List<CourseMemberDTO> = listOf(),
)
data class CourseMemberDTO(
    val username: String,
    val role: CourseMemberRole,
    val createdAt: LocalDateTime,
)

data class CreateCourseRequest(
    val name: String,
    val photoUrl: String? = null
)
data class CreateCourseResponse(
    val courseId: Long,
    val message: String
)

data class DeleteCourseResponse(
    val courseId: Long,
    val message: String
)

data class UserCoursesResponse(
    val username: String,
    val courses: List<CourseDTO> = listOf(),
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