package com.example.teamroomback.dtos

import com.example.teamroomback.validation.ValidPassword
import com.example.teamroomback.validation.ValidUsername
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @field:Schema(
        description = "Логін користувача (унікальний); не містить пробілів. Довжина: 4-32 символи.",
        example = "nek1t_user",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 4,
        maxLength = 32,
    )
    @field:NotBlank(message = "Username cannot be empty")
    @field:Size(min = 4, max = 32, message = "Username must be 4 to 32 characters long")
    @field:ValidUsername
    val username: String,

    @field:Schema(
        description = "Email користувача (унікальний). Довжина: 3-254 символи.",
        example = "nek1t@gmail.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 3,
        maxLength = 254,
        format = "email"
    )
    @field:NotBlank(message = "Email cannot be empty")
    @field:Email(message = "Invalid email format")
    @field:Size(min = 3, max = 254, message = "Email must be between 3 and 254 characters")
    val email: String,

    @field:Schema(
        description = "Пароль користувача. Повинен містити 1 велику літеру, 1 малу літеру, 1 цифру та 1 спец. символ. Довжина: 8-100 символів.",
        example = "Password123$",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8,
        maxLength = 100,
        writeOnly = true
    )
    @field:NotBlank(message = "Password cannot be empty")
    @field:Size(min = 8, max = 100, message = "Password must be 8 to 100 characters long")
    @field:ValidPassword
    val password: String
)

data class RegisterResponse(
    @field:Schema(description = "Повідомлення про статус операції.")
    val message: String,

    @field:Schema(description = "Логін успішно зареєстрованого користувача.")
    val username: String? = null
)

data class LoginRequest(
    @field:Schema(
        description = "Логін користувача.",
        example = "nek1t_user"
    )
    @field:NotBlank(message = "Username cannot be empty")
    val username: String,

    @field:Schema(
        description = "Пароль користувача.",
        example = "Password123$",
        writeOnly = true
    )
    @field:NotBlank(message = "Password cannot be empty")
    val password: String
)

data class LoginResponse(
    @field:Schema(
        description = "JWT Bearer Token, необхідний для наступних захищених запитів.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val jwt: String,

    @field:Schema(description = "Логін аутентифікованого користувача.")
    val username: String
)
