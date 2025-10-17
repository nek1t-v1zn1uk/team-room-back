package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema

data class SimpleMessageResponse(
    @field:Schema(
        description = "Загальне повідомлення про результат операції.",
        example = "Операція виконана успішно."
    )
    val message: String,
)