package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.DownloadLinkResponse
import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.UploadLinkResponse
import com.example.teamroomback.services.PCloudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cloud-storage")
@Tag(name = "Хмарне сховище", description = "Ендпоїнти для взаємодії з хмарним сховищем файлів (pCloud).")
@SecurityRequirement(name = "bearerAuth")
class PCloudController(
    private val service: PCloudService
) {

    @GetMapping("/get-upload-link")
    @Operation(
        summary = "Отримати посилання для вивантаження файлу на хмару.",
        description = "Генерує унікальне посилання (upload link) для завантаження файлу в хмару."
    )
    @ApiResponse(responseCode = "200", description = "Посилання успішно згенеровано.", content = [Content(schema = Schema(implementation = UploadLinkResponse::class))])
    @ApiResponse(responseCode = "400", description = "Неправильний або відсутній параметр 'purpose'.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Помилка на боці хмарного сервісу або сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getUploadLink(
        @Parameter(description = "Призначення файлу, наприклад, 'profile-photo', для організації файлу у хмарі.", example = "profile-photo", required = true)
        @RequestParam purpose: String
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(UploadLinkResponse(
            link = service.getUploadLink(purpose)
        ))
    }

    @GetMapping("/get-public-link")
    @Operation(
        summary = "Отримати користувацьке посилання для файл.",
        description = "Генерує користувацьке посилання для завантаження файлу за його ідентифікатором."
    )
    @ApiResponse(responseCode = "200", description = "Посилання успішно отримано.", content = [Content(schema = Schema(implementation = DownloadLinkResponse::class))])
    @ApiResponse(responseCode = "400", description = "Неправильний або відсутній параметр 'fileid'.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Файл з вказаним 'fileid' не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Помилка на боці хмарного сервісу або сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getPublicLink(
        @Parameter(description = "Цифровий ідентифікатор файлу в хмарному сховищі.", example = "1234567890", required = true)
        @RequestParam fileid: Long
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(DownloadLinkResponse(
            link = service.getPubLink(fileid)
        ))
    }
}