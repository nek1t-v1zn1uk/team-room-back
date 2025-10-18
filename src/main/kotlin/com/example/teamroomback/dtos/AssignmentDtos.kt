package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Schema(description = "DTO для створення нового завдання")
data class CreateAssignmentRequest(
    @Schema(description = "Назва завдання", example = "Лабораторна робота №1")
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String,

    @Schema(description = "Детальний опис завдання", example = "Виконати завдання згідно методичних вказівок")
    val description: String?,

    @Schema(description = "Максимальна оцінка за завдання", example = "100")
    val maxGrade: Int = 100,

    @Schema(description = "Кінцевий термін здачі завдання", example = "2024-12-31T23:59:59")
    @field:NotNull(message = "Deadline is required")
    @field:Future(message = "Deadline must be in the future")
    val deadline: LocalDateTime
)

@Schema(description = "DTO для повного оновлення завдання (PUT)")
data class PutAssignmentRequest(
    @Schema(description = "Нова назва завдання", example = "Оновлена лабораторна робота №1")
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String,

    @Schema(description = "Новий детальний опис завдання", example = "Виконати завдання згідно оновлених методичних вказівок")
    val description: String?,

    @Schema(description = "Нова максимальна оцінка за завдання", example = "120")
    val maxGrade: Int,

    @Schema(description = "Новий кінцевий термін здачі завдання", example = "2025-01-15T23:59:59")
    @field:NotNull(message = "Deadline is required")
    @field:Future(message = "Deadline must be in the future")
    val deadline: LocalDateTime
)

@Schema(description = "DTO для часткового оновлення завдання (PATCH)")
data class PatchAssignmentRequest(
    @Schema(description = "Нова назва завдання", example = "Виправлена лабораторна робота №1")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String?,

    @Schema(description = "Новий детальний опис завдання", example = "Додано примітки до завдання")
    val description: String?,

    @Schema(description = "Нова максимальна оцінка за завдання", example = "105")
    val maxGrade: Int?,

    @Schema(description = "Новий кінцевий термін здачі завдання", example = "2025-01-20T23:59:59")
    @field:Future(message = "Deadline must be in the future")
    val deadline: LocalDateTime?
)

@Schema(description = "DTO для додавання медіафайлу до завдання")
data class AddAssignmentMediaRequest(
    @Schema(description = "Назва медіафайлу", example = "Методичні вказівки.pdf")
    @field:NotBlank(message = "Name is required")
    val name: String,
    @Schema(description = "URL-адреса файлу", example = "https://example.com/files/method.pdf")
    @field:NotBlank(message = "File URL is required")
    val fileUrl: String
)

@Schema(description = "DTO для перейменування медіафайлу")
data class RenameAssignmentMediaRequest(
    @Schema(description = "Нова назва медіафайлу", example = "Оновлені методичні вказівки.pdf")
    @field:NotBlank(message = "Name is required")
    val name: String
)

@Schema(description = "Відповідь, що містить ID створеного завдання")
data class CreateAssignmentResponse(val id: Long, val message: String)

@Schema(description = "Відповідь для операцій з медіафайлами завдання")
data class AssignmentMediaResponse(val id: Long, val assignmentId: Long, val message: String)

@Schema(description = "Скорочена інформація про завдання для списків")
data class AssignmentShortDTO(
    @Schema(description = "Унікальний ідентифікатор завдання", example = "1")
    val id: Long,
    @Schema(description = "Назва завдання", example = "Лабораторна робота №1")
    val title: String,
    @Schema(description = "Максимальна оцінка за завдання", example = "100")
    val maxGrade: Int,
    @Schema(description = "Дата створення завдання", example = "2024-10-20T10:00:00")
    val createdAt: LocalDateTime,
    @Schema(description = "Кінцевий термін здачі завдання", example = "2024-12-31T23:59:59")
    val deadline: LocalDateTime,
    @Schema(description = "Ім'я користувача, який створив завдання", example = "nek1t")
    val authorUsername: String
)

@Schema(description = "Повна інформація про завдання")
data class AssignmentDTO(
    @Schema(description = "Унікальний ідентифікатор завдання", example = "1")
    val id: Long,
    @Schema(description = "Назва завдання", example = "Лабораторна робота №1")
    val title: String,
    @Schema(description = "Детальний опис завдання", example = "Виконати завдання згідно методичних вказівок")
    val description: String?,
    @Schema(description = "Максимальна оцінка за завдання", example = "100")
    val maxGrade: Int,
    @Schema(description = "Дата створення завдання", example = "2024-10-20T10:00:00")
    val createdAt: LocalDateTime,
    @Schema(description = "Кінцевий термін здачі завдання", example = "2024-12-31T23:59:59")
    val deadline: LocalDateTime,
    @Schema(description = "Ім'я користувача, який створив завдання", example = "nek1t")
    val authorUsername: String,
    @Schema(description = "Список прикріплених медіафайлів")
    val media: List<AssignmentMediaDTO>
)

@Schema(description = "Інформація про медіафайл")
data class AssignmentMediaDTO(
    @Schema(description = "Унікальний ідентифікатор медіафайлу", example = "1")
    val id: Long,
    @Schema(description = "Назва медіафайлу", example = "Методичні вказівки.pdf")
    val name: String?,
    @Schema(description = "URL-адреса файлу", example = "https://example.com/files/method.pdf")
    val fileUrl: String?
)