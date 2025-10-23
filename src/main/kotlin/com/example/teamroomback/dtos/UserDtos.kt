package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema

data class UserResponse(
    @field:Schema(
        description = "Логін користувача.",
        example = "nek1t_user"
    )
    val username: String,
    @field:Schema(
        description = "Ел. пошта користувача.",
        example = "nek1t@gmail.com"
    )
    val email: String,
    @field:Schema(
        description = "Профіль користувача.",
    )
    val profile: GetProfileResponse
)
data class UsersListResponse(
    @field:Schema(
        description = "Список користувачів.",
    )
    val users: List<UserResponse> = listOf()
)