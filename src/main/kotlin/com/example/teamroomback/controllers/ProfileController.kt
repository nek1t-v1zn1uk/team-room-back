package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.CreateProfileRequest
import com.example.teamroomback.dtos.CreateProfileResponse
import com.example.teamroomback.dtos.GetProfileResponse
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.entities.Profile
import com.example.teamroomback.services.ProfileService
import jakarta.validation.Valid
import org.apache.tomcat.util.net.openssl.ciphers.Authentication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val profileService: ProfileService,
) {

    @PostMapping
    fun createProfile(@Valid @RequestBody request: CreateProfileRequest): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication
            profileService.createProfile(
                username = authentication.name,
                firstName = request.firstName,
                lastName = request.lastName,
                biography = request.biography,
                photoUrl = request.photoUrl,
            )
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
    fun getMyProfile(): ResponseEntity<Any> {
        val authentication = SecurityContextHolder.getContext().authentication
        return try {
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
}