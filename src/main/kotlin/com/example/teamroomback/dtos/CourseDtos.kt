package com.example.teamroomback.dtos

import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMemberRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
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
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 100)
    val name: String,
    val photoUrl: String? = null
)
data class CreateCourseResponse(
    val courseId: Long,
    val message: String
)

data class PutCourseRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 100)
    val name: String,
    val photoUrl: String? = null
)
data class PutCourseResponse(
    val courseId: Long,
    val message: String
)

data class PatchCourseRequest(
    @field:Size(max = 100)
    val name: String? = null,
    val photoUrl: String? = null
)
data class PatchCourseResponse(
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
    @NotBlank(message = "Username cannot be empty")
    @field:Size(min = 4, max = 32, message = "Username must be 4 to 32 characters long")
    val username: String,
    @NotBlank(message = "Role cannot be empty")
    val role: CourseMemberRole
)
data class AddCourseMemberResponse(
    val username: String,
    val courseId: Long,
    val message: String
)

data class PutCourseMemberRoleRequest(
    @NotBlank(message = "Username cannot be empty")
    @field:Size(min = 4, max = 32, message = "Username must be 4 to 32 characters long")
    val username: String,
    val role: CourseMemberRole,
)
data class PutCourseMemberRoleResponse(
    val username: String,
    val newRole: CourseMemberRole,
    val message: String
)

data class DeleteCourseMemberResponse(
    val username: String,
    val message: String
)