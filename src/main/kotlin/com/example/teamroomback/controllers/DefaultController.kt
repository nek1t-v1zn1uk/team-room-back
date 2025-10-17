package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Collections

@RestController
@RequestMapping("/api")
@Tag(name = "Базові Тести Доступу", description = "Прості ендпоїнти для тестування публічного та захищеного доступу.")
class DefaultController {

    @GetMapping("/no-auth")
    @Operation(
        summary = "Публічний доступ.",
        description = "Ендпоїнт доступний будь-кому без JWT-токена.",
        security = []
    )
    @ApiResponse(
        responseCode = "200",
        description = "Успішна відповідь. Повідомлення 'Hello World!' повернено.",
        content = [Content(schema = Schema(implementation = Map::class))]
    )
    @ApiResponse(
        responseCode = "500",
        description = "Непередбачена помилка на сервері.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    fun sayHelloToEveryone(): Map<String, String> {
        return Collections.singletonMap("message", "Hello World!")
    }


    @GetMapping("/with-auth")
    @Operation(
        summary = "Захищений доступ.",
        description = "Ендпоїнт доступний тільки аутентифікованим користувачам (з дійсним JWT-токеном)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Успішна відповідь. Повідомлення 'Hello World!' повернено.",
        content = [Content(schema = Schema(implementation = Map::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "Неавторизовано. JWT-токен відсутній або недійсний.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "403",
        description = "Доступ заборонено. Токен дійсний, але недостатньо прав.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "500",
        description = "Непередбачена помилка на сервері.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    fun sayHelloToUsersOnly(): Map<String, String> {
        return Collections.singletonMap("message", "Hello World!")
    }

}