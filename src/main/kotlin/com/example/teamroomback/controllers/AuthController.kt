package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.LoginRequest
import com.example.teamroomback.dtos.LoginResponse
import com.example.teamroomback.dtos.RegisterRequest
import com.example.teamroomback.dtos.RegisterResponse
import com.example.teamroomback.services.UserService
import com.example.teamroomback.utils.JwtUtils
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
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils
) {

    @PostMapping("/register")
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