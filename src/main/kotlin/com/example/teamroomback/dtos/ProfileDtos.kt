package com.example.teamroomback.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateProfileRequest(
    @field:NotBlank(message = "First name cannot be empty")
    @field:Size(max = 32, message = "First name must be up to 32 symbols")
    val firstName: String,

    @field:Size(max = 32, message = "Last name must be up to 32 symbols")
    val lastName: String? = null,

    @field:Size(max = 100, message = "Biography must be up to 100 symbols")
    val biography: String? = null,

    val photoUrl: String? = null
)
data class CreateProfileResponse(
    val message: String,
)

data class GetProfileResponse(
    val firstName: String,
    val lastName: String? = null,
    val biography: String? = null,
    val photoUrl: String? = null
)

data class PutProfileRequest(
    @field:NotBlank(message = "First name cannot be empty")
    @field:Size(max = 32, message = "First name must be up to 32 symbols")
    val firstName: String,

    @field:Size(max = 32, message = "Last name must be up to 32 symbols")
    val lastName: String? = null,

    @field:Size(max = 100, message = "Biography must be up to 100 symbols")
    val biography: String? = null,

    val photoUrl: String? = null
)
data class PatchProfileRequest(
    @field:Size(max = 32, message = "First name must be up to 32 symbols")
    val firstName: String? = null,

    @field:Size(max = 32, message = "Last name must be up to 32 symbols")
    val lastName: String? = null,

    @field:Size(max = 100, message = "Biography must be up to 100 symbols")
    val biography: String? = null,

    val photoUrl: String? = null
)
data class UpdateProfileResponse(
    val message: String,
)
