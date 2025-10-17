package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.CreateProfileRequest
import com.example.teamroomback.dtos.CreateProfileResponse
import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.GetProfileResponse
import com.example.teamroomback.dtos.PatchProfileRequest
import com.example.teamroomback.dtos.PutProfileRequest
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.UpdateProfileResponse
import com.example.teamroomback.services.ProfileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Профіль користувача", description = "Ендпоїнти для створення, отримання та оновлення профілю користувача.")
@SecurityRequirement(name = "bearerAuth")
class ProfileController(
    private val profileService: ProfileService,
) {

    @PostMapping
    @Operation(
        summary = "Створити профіль для поточного користувача.",
        description = "Створює запис профілю (ім'я, біографія, фото), пов'язаний з аутентифікованим користувачем.",
    )
    @ApiResponse(responseCode = "200", description = "Профіль успішно створено.", content = [Content(schema = Schema(implementation = CreateProfileResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації даних.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "409", description = "Профіль для цього користувача вже існує.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun createProfile(@Valid @RequestBody request: CreateProfileRequest): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication
            profileService.createProfile(username = authentication.name, request)

            ResponseEntity.ok(
                CreateProfileResponse(
                    message = "Profile successfully created",
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Profile creation failed: ${e.message}.",
                )
            )
        }
    }

    @GetMapping
    @Operation(
        summary = "Отримати профіль поточного користувача.",
        description = "Повертає дані профілю для аутентифікованого користувача.",
    )
    @ApiResponse(responseCode = "200", description = "Профіль успішно отримано.", content = [Content(schema = Schema(implementation = GetProfileResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Профіль для поточного користувача не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getMyProfile(): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            val profile = profileService.getProfile(authentication.name)
            ResponseEntity.ok(
                GetProfileResponse(
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    biography = profile.biography,
                    photoUrl = profile.photoUrl,
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Profile searching and sharing failed: ${e.message}"
                )
            )
        }
    }

    @GetMapping("/{username}")
    @Operation(
        summary = "Отримати профіль користувача за його логіном.",
        description = "Повертає публічні дані профілю для будь-якого користувача системи.",
    )
    @ApiResponse(responseCode = "200", description = "Профіль успішно отримано.", content = [Content(schema = Schema(implementation = GetProfileResponse::class))])
    @ApiResponse(responseCode = "404", description = "Користувача або його профіль не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getUserProfile(@PathVariable username: String): ResponseEntity<Any> {
        return try {
            val profile = profileService.getProfile(username)

            ResponseEntity.ok(
                GetProfileResponse(
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    biography = profile.biography,
                    photoUrl = profile.photoUrl,
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Profile searching and sharing failed: ${e.message}"
                )
            )
        }
    }

    @PutMapping
    @Operation(
        summary = "Повністю оновити профіль поточного користувача.",
        description = "Замінює всі дані профілю на нові. Поля, які не передані (напр. lastName), будуть очищені (стануть null).",
    )
    @ApiResponse(responseCode = "200", description = "Профіль успішно оновлено.", content = [Content(schema = Schema(implementation = UpdateProfileResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Профіль для оновлення не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun putProfile(@Valid @RequestBody request: PutProfileRequest): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            profileService.putProfile(authentication.name, request)

            ResponseEntity.ok(
                UpdateProfileResponse(
                    message = "Profile successfully updated",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Profile putting failed: ${e.message}"
                )
            )
        }
    }

    @PatchMapping
    @Operation(
        summary = "Частково оновити профіль поточного користувача.",
        description = "Оновлює лише передані в запиті поля профілю. Поля, які не передані, залишаться без змін.",
    )
    @ApiResponse(responseCode = "200", description = "Профіль успішно оновлено.", content = [Content(schema = Schema(implementation = UpdateProfileResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Профіль для оновлення не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun patchProfile(@Valid @RequestBody request: PatchProfileRequest): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            profileService.patchProfile(authentication.name, request)

            ResponseEntity.ok(
                UpdateProfileResponse(
                    message = "Profile successfully updated",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Profile patching failed: ${e.message}"
                )
            )
        }
    }
}