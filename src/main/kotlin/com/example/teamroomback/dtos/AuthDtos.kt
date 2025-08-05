package com.example.teamroomback.dtoss

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @field:NotBlank(message = "Username cannot be empty")
    @field:Size(min = 4, max = 32, message = "Username must be 4 to 32 characters long")
    val username: String,

    @field:NotBlank(message = "Email cannot be empty")
    @field:Email(message = "Invalid email format")
    @field:Size(min = 3, max = 254, message = "Email must be between 3 and 254 characters")
    val email: String,

    @field:NotBlank(message = "Password cannot be empty")
    @field:Size(min = 6, max = 100, message = "Password must be 6 to 100 characters long")
    val password: String
)

data class RegisterResponse(
    val message: String,
    val username: String? = null
)

data class LoginRequest(
    @field:NotBlank(message = "Username cannot be empty")
    val username: String,

    @field:NotBlank(message = "Password cannot be empty")
    val password: String
)

data class LoginResponse(
    val jwt: String,
    val username: String
)
