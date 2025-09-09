package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.CreateProfileRequest
import com.example.teamroomback.dtos.CreateProfileResponse
import com.example.teamroomback.dtos.GetProfileResponse
import com.example.teamroomback.dtos.PatchProfileRequest
import com.example.teamroomback.dtos.PutProfileRequest
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.UpdateProfileResponse
import com.example.teamroomback.services.ProfileService
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
class ProfileController(
    private val profileService: ProfileService,
) {

    @PostMapping
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