package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorResponse(
    @field:Schema(
        description = "Мітка часу виникнення помилки",
        example = "2025-10-17T14:30:00.123456",
        format = "date-time"
    )
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @field:Schema(
        description = "HTTP статус код (наприклад, 400, 403, 409, 500)",
        example = "400"
    )
    val status: Int,

    @field:Schema(
        description = "Причина HTTP статусу (наприклад, 'Bad Request', 'Conflict')",
        example = "Bad Request"
    )
    val error: String,

    @field:Schema(
        description = "Основне повідомлення про помилку.",
        example = "Validation failed for one or more fields."
    )
    val message: String?,

    @field:Schema(
        description = "Деталі помилки валідації (присутні лише для 400 Bad Request).",
        example = """[{"field": "username", "message": "Username must be 4 to 32 characters long"}]""",
        nullable = true
    )
    val details: List<Map<String, String>>? = null
)