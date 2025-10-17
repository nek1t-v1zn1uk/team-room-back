package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.LoginRequest
import com.example.teamroomback.dtos.LoginResponse
import com.example.teamroomback.dtos.RegisterRequest
import com.example.teamroomback.dtos.RegisterResponse
import com.example.teamroomback.services.UserService
import com.example.teamroomback.utils.JwtUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентифікація", description = "Ендпоїнти для реєстрації та входу/отримання JWT-токенів.")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils
) {

    @PostMapping("/register")
    @Operation(
        summary = "Зареєструвати нового користувача.",
        description = "Створює нового користувача з унікальним логіном та email.",
        security = []
    )
    @ApiResponse(
        responseCode = "200",
        description = "Успішна реєстрація.",
        content = [Content(schema = Schema(implementation = RegisterResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Конфлікт. Користувач з таким логіном або email вже існує.",
        content = [Content(schema = Schema(implementation = RegisterResponse::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "Помилка валідації вхідних даних або некоректний JSON.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "500",
        description = "Непередбачена помилка на сервері.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        return try {
            val user = userService.registerUser(username = request.username, request.email, request.password)
            ResponseEntity.ok(RegisterResponse("User registered successfully!", user.username))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(RegisterResponse(e.message ?: "Username or email already exists"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RegisterResponse("Registration failed: ${e.message}"))
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "Аутентифікація користувача та отримання JWT-токена.",
        description = "Перевіряє облікові дані та повертає JWT-токен для доступу до захищених ресурсів.",
        security = []
    )
    @ApiResponse(
        responseCode = "200",
        description = "Успішна аутентифікація. Токен повернено.",
        content = [Content(schema = Schema(implementation = LoginResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "Невірний логін або пароль.",
        content = [Content(schema = Schema(implementation = LoginResponse::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "Помилка валідації вхідних даних або некоректний JSON.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "500",
        description = "Непередбачена помилка на сервері.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
            val userDetails = authentication.principal as UserDetails
            val token = jwtUtils.generateToken(userDetails)

            ResponseEntity.ok(LoginResponse(token, userDetails.username))
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse("", "Invalid username or password"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse("", "Login failed: ${e.message}"))
        }
    }
}