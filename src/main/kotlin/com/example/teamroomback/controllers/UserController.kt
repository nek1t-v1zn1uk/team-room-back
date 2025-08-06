package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
) {

    @DeleteMapping
    fun deleteUser(): ResponseEntity<Any> {
        return try{
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
}