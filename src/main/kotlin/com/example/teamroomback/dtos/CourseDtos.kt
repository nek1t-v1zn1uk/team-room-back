package com.example.teamroomback.dtos

import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMemberRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CourseDTO(
    @field:Schema(description = "Унікальний ідентифікатор курсу.", example = "101")
    val id: Long,

    @field:Schema(description = "Назва курсу.", example = "Комп'ютерні мережі")
    val name: String,

    @field:Schema(description = "URL-адреса обкладинки курсу.", example = "https://example.com/course_cover.jpg", format = "uri", nullable = true)
    val photoUrl: String? = null,

    @field:Schema(description = "Прапорець, що вказує, чи є курс відкритим.", example = "true")
    val isOpen: Boolean,

    @field:Schema(description = "К-сть учасників курсу.")
    val membersCount: Int,

    @field:Schema(description = "Список учасників курсу.")
    val members: List<CourseMemberDTO> = listOf(),
)

data class CourseMemberDTO(
    @field:Schema(description = "Логін учасника курсу.", example = "student123")
    val username: String,

    @field:Schema(description = "Роль учасника в курсі.", example = "STUDENT")
    val role: CourseMemberRole,

    @field:Schema(description = "Час приєднання до курсу.", example = "2025-10-17T18:30:00", format = "date-time")
    val createdAt: LocalDateTime,
)

data class CreateCourseRequest(
    @field:Schema(description = "Назва нового курсу.", example = "Основи програмування", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 100)
    val name: String,

    @field:Schema(description = "URL-адреса обкладинки курсу.", example = "https://example.com/new_course.jpg", format = "uri", nullable = true)
    val photoUrl: String? = null
)

data class CreateCourseResponse(
    @field:Schema(description = "ID створеного курсу.", example = "102")
    val courseId: Long,

    @field:Schema(description = "Повідомлення про результат.", example = "Курс успішно створено.")
    val message: String
)

data class PutCourseRequest(
    @field:Schema(description = "Нова назва курсу.", example = "Алгоритми та структури даних", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 100)
    val name: String,

    @field:Schema(description = "Нова URL-адреса обкладинки курсу.", example = "https://example.com/updated_course.jpg", format = "uri", nullable = true)
    val photoUrl: String? = null
)

data class PutCourseResponse(
    @field:Schema(description = "ID оновленого курсу.", example = "102")
    val courseId: Long,

    @field:Schema(description = "Повідомлення про результат.", example = "Курс успішно оновлено.")
    val message: String
)

data class PatchCourseRequest(
    @field:Schema(description = "Нова назва курсу.", example = "Просунуті алгоритми", maxLength = 100, nullable = true)
    @field:Size(max = 100)
    val name: String? = null,

    @field:Schema(description = "Нова URL-адреса обкладинки курсу.", example = "https://example.com/patched_course.jpg", format = "uri", nullable = true)
    val photoUrl: String? = null
)

data class PatchCourseResponse(
    @field:Schema(description = "ID оновленого курсу.", example = "102")
    val courseId: Long,

    @field:Schema(description = "Повідомлення про результат.", example = "Курс успішно оновлено.")
    val message: String
)

data class DeleteCourseResponse(
    @field:Schema(description = "ID видаленого курсу.", example = "102")
    val courseId: Long,

    @field:Schema(description = "Повідомлення про результат.", example = "Курс було успішно видалено.")
    val message: String
)

data class UserCoursesResponse(
    @field:Schema(description = "Логін користувача, чиї курси перераховано.", example = "teacher_smith")
    val username: String,

    @field:Schema(description = "Список курсів, до яких належить користувач.")
    val courses: List<CourseDTO> = listOf(),
)

data class AddCourseMemberRequest(
    @field:Schema(description = "Логін користувача, якого потрібно додати.", example = "new_student_25", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "Username cannot be empty")
    @field:Size(min = 4, max = 32, message = "Username must be 4 to 32 characters long")
    val username: String,

    @field:Schema(description = "Роль, яку потрібно призначити користувачу.", example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
    val role: CourseMemberRole
)

data class AddCourseMemberResponse(
    @field:Schema(description = "Логін доданого користувача.", example = "new_student_25")
    val username: String,

    @field:Schema(description = "ID курсу, до якого додали користувача.", example = "102")
    val courseId: Long,

    @field:Schema(description = "Повідомлення про результат.", example = "Користувача успішно додано до курсу.")
    val message: String
)

data class PutCourseMemberRoleRequest(
    @field:Schema(description = "Логін користувача, чию роль потрібно змінити.", example = "student123", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "Username cannot be empty")
    @field:Size(min = 4, max = 32, message = "Username must be 4 to 32 characters long")
    val username: String,

    @field:Schema(description = "Нова роль для користувача.", example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
    val role: CourseMemberRole,
)

data class PutCourseMemberRoleResponse(
    @field:Schema(description = "Логін користувача, чию роль змінили.", example = "student123")
    val username: String,

    @field:Schema(description = "Нова роль користувача.", example = "STUDENT")
    val newRole: CourseMemberRole,

    @field:Schema(description = "Повідомлення про результат.", example = "Роль користувача було успішно оновлено.")
    val message: String
)

data class DeleteCourseMemberResponse(
    @field:Schema(description = "Логін видаленого користувача.", example = "student123")
    val username: String,

    @field:Schema(description = "Повідомлення про результат.", example = "Користувача було успішно видалено з курсу.")
    val message: String
)

data class CreateCourseChatRequest(
    @field:NotBlank(message = "Chat name cannot be blank")
    @field:Size(min = 1, max = 255, message = "Chat name must be between 1 and 255 characters")
    val name: String,
    val photoUrl: String? = null,
)