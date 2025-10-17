package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateProfileRequest(
    @field:Schema(
        description = "Ім'я користувача.",
        example = "Никита",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 32
    )
    @field:NotBlank(message = "First name cannot be empty")
    @field:Size(max = 32, message = "First name must be up to 32 symbols")
    val firstName: String,

    @field:Schema(
        description = "Прізвище користувача.",
        example = "Візнюк",
        maxLength = 32,
        nullable = true
    )
    @field:Size(max = 32, message = "Last name must be up to 32 symbols")
    val lastName: String? = null,

    @field:Schema(
        description = "Коротка біографія або статус.",
        example = "Kotlin Spring software engineer",
        maxLength = 100,
        nullable = true
    )
    @field:Size(max = 100, message = "Biography must be up to 100 symbols")
    val biography: String? = null,

    @field:Schema(
        description = "URL-адреса фотографії профілю.",
        example = "https://e.pcloud.link/publink/show?code=XZgcMOZtvkkzRNjFMY7ufRRFPA5LRYMxfjk",
        format = "uri",
        nullable = true
    )
    val photoUrl: String? = null
)

data class CreateProfileResponse(
    @field:Schema(
        description = "Повідомлення про успішне створення профілю.",
        example = "Profile successfully created"
    )
    val message: String,
)

data class GetProfileResponse(
    @field:Schema(
        description = "Ім'я користувача.",
        example = "Никита"
    )
    val firstName: String,

    @field:Schema(
        description = "Прізвище користувача.",
        example = "Візнюк",
        nullable = true
    )
    val lastName: String? = null,

    @field:Schema(
        description = "Біографія.",
        example = "Kotlin Spring software engineer",
        nullable = true
    )
    val biography: String? = null,

    @field:Schema(
        description = "URL-адреса аватара.",
        example = "https://e.pcloud.link/publink/show?code=XZgcMOZtvkkzRNjFMY7ufRRFPA5LRYMxfjk",
        format = "uri",
        nullable = true
    )
    val photoUrl: String? = null
)

data class PutProfileRequest(
    @field:Schema(
        description = "Ім'я користувача.",
        example = "Никита",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 32
    )
    @field:NotBlank(message = "First name cannot be empty")
    @field:Size(max = 32, message = "First name must be up to 32 symbols")
    val firstName: String,

    @field:Schema(
        description = "Прізвище користувача.",
        example = "Візнюк",
        maxLength = 32,
        nullable = true
    )
    @field:Size(max = 32, message = "Last name must be up to 32 symbols")
    val lastName: String? = null,

    @field:Schema(
        description = "Біографія.",
        example = "Kotlin Spring software engineer",
        maxLength = 100,
        nullable = true
    )
    @field:Size(max = 100, message = "Biography must be up to 100 symbols")
    val biography: String? = null,

    @field:Schema(
        description = "URL-адреса аватара.",
        example = "https://e.pcloud.link/publink/show?code=XZgcMOZtvkkzRNjFMY7ufRRFPA5LRYMxfjk",
        format = "uri",
        nullable = true
    )
    val photoUrl: String? = null
)

data class PatchProfileRequest(
    @field:Schema(
        description = "Ім'я користувача.",
        example = "Никита",
        maxLength = 32,
        nullable = true
    )
    @field:Size(max = 32, message = "First name must be up to 32 symbols")
    val firstName: String? = null,

    @field:Schema(
        description = "Прізвище користувача.",
        example = "Візнюк",
        maxLength = 32,
        nullable = true
    )
    @field:Size(max = 32, message = "Last name must be up to 32 symbols")
    val lastName: String? = null,

    @field:Schema(
        description = "Біографія.",
        example = "Kotlin Spring software engineer",
        maxLength = 100,
        nullable = true
    )
    @field:Size(max = 100, message = "Biography must be up to 100 symbols")
    val biography: String? = null,

    @field:Schema(
        description = "URL-адреса аватара.",
        example = "https://e.pcloud.link/publink/show?code=XZgcMOZtvkkzRNjFMY7ufRRFPA5LRYMxfjk",
        format = "uri",
        nullable = true
    )
    val photoUrl: String? = null
)

data class UpdateProfileResponse(
    @field:Schema(
        description = "Повідомлення про успішне оновлення профілю.",
        example = "Profile successfully updated"
    )
    val message: String,
)
