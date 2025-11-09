package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.CreateConferenceRequest
import com.example.teamroomback.services.ConferenceService
import com.example.teamroomback.services.JitsiJwtService
import com.example.teamroomback.services.UserService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/course/{courseId}/conferences")
@Tag(name = "Курси", description = "Ендпоїнти для керування курсами та їх учасниками.")
@Tag(name = "Конференції", description = "Ендпоїнти для керування конференціями.")
@SecurityRequirement(name = "bearerAuth")
class ConferenceController(
    private val conferenceService: ConferenceService
) {

    @PostMapping()
    fun startConference(@PathVariable courseId: Long, @RequestBody request: CreateConferenceRequest): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val conferenceDto = conferenceService.createConference(username, courseId, request)

            ResponseEntity.ok().body(conferenceDto)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conference creation failed: ${e.message}")
        }
    }

    @PostMapping("/{conferenceId}")
    fun joinConference(@PathVariable courseId: Long, @PathVariable conferenceId: Long): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val joinConferenceDetails = conferenceService.getConferenceJoinDetails(username, courseId, conferenceId)

            ResponseEntity.ok(joinConferenceDetails)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conference was not found: ${e.message}")
        }
    }

    @GetMapping()
    fun getConferences(@PathVariable courseId: Long): ResponseEntity<Any> {
        return try {
            val conferences = conferenceService.getCourseConferencesDTOs(courseId)

            ResponseEntity.ok(conferences)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conferences was not found: ${e.message}")
        }
    }

    @GetMapping("/{conferenceId}")
    fun getConference(@PathVariable courseId: Long, @PathVariable conferenceId: Long): ResponseEntity<Any> {
        return try {
            val conferenceDto = conferenceService.getConferenceDTO(conferenceId)

            ResponseEntity.ok(conferenceDto)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conference was not found: ${e.message}")
        }
    }

}