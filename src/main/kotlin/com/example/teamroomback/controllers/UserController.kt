package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.*
import com.example.teamroomback.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Користувач", description = "Операції, пов'язані з поточним аутентифікованим користувачем.")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val userService: UserService,
) {

    @DeleteMapping("/api/user")
    @Operation(summary = "Видалити поточного користувача.", description = "Видаляє профіль, дані та всі пов'язані записи для аутентифікованого користувача. Ця дія є незворотною.",)
    @ApiResponse(responseCode = "200", description = "Користувача успішно видалено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований. Потрібно надати дійсний JWT токен.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено. Недостатньо прав для виконання операції.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteUser(): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            userService.deleteUser(authentication.name)

            ResponseEntity.ok(
                SimpleMessageResponse(
                    "User successfully deleted."
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    "User deletion failed: ${e.message}."
                )
            )
        }
    }

    @GetMapping("/api/user/search")
    @Operation(summary = "Шукати користувачів за частиною логіну.", description = "Повертає список усіх користувачів, чий логін містить вказаний текст. Ігнорується регістр тексту. Якщо таких користувачів немає, поверне пустий список.",)
    @ApiResponse(responseCode = "200", description = "Користувачів успішно повернуто.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований. Потрібно надати дійсний JWT токен.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено. Недостатньо прав для виконання операції.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun findUsersByPartialUsername(
        @Parameter(description = "Частина логіну за якою потрібно шукати.") @RequestParam partialUsername: String
    ): ResponseEntity<Any> {
        return try {
            val users = userService.finUsersByPartialUsername(partialUsername)


            ResponseEntity.ok(
                UsersListResponse(
                    users.map {
                        UserResponse(
                            it.usernameValue,
                            it.email,
                            GetProfileResponse(
                                it.profile!!.firstName,
                                it.profile!!.lastName,
                                it.profile!!.biography,
                                photoUrl = it.profile!!.photoUrl
                            )
                        )
                    }
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    "Users search failed: ${e.message}."
                )
            )
        }
    }
}