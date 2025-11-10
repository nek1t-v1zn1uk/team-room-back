package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ConferenceDTO
import com.example.teamroomback.dtos.ConferenceJoinDetails
import com.example.teamroomback.dtos.CreateConferenceRequest
import com.example.teamroomback.dtos.CreateMaterialResponse
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.services.ConferenceService
import com.example.teamroomback.services.JitsiJwtService
import com.example.teamroomback.services.UserService
import com.example.teamroomback.validation.CourseOpenStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Почати конференцію у курсі.", description = "Надає дані для приєднання до нової конференції. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Деталі для старту конференції успішно створено.", content = [Content(schema = Schema(implementation = ConferenceJoinDetails::class))])
    @ApiResponse(responseCode = "404", description = "Деталі для старту конференції неможливо створити.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun startConference(@PathVariable courseId: Long, @RequestBody request: CreateConferenceRequest): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val conferenceDto = conferenceService.createConference(username, courseId, request)

            ResponseEntity.ok().body(conferenceDto)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse("Conference creation failed: ${e.message}"))
        }
    }

    @PostMapping("/{conferenceId}")
    @PreAuthorize("hasPermission(#courseId, 'VIEWER')")
    @CourseOpenStatus
    @Operation(summary = "Приєднатися до конференції у курсі.", description = "Надає дані для приєднання до існуючої конференції. Не можна одночасно двічі приєднатися до конфи. Потребує ролі не нижче VIEWER. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Деталі для старту конференції успішно створено.", content = [Content(schema = Schema(implementation = ConferenceJoinDetails::class))])
    @ApiResponse(responseCode = "404", description = "Деталі для старту конференції неможливо створити.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "400", description = "Неможливо приєднатися до конференції.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun joinConference(@PathVariable courseId: Long, @PathVariable conferenceId: Long): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val joinConferenceDetails = conferenceService.getConferenceJoinDetails(username, courseId, conferenceId)

            ResponseEntity.ok(joinConferenceDetails)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse("Conference was not found: ${e.message}"))
        } catch(e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse("Joining impossible: ${e.message}"))
        }
    }

    @GetMapping()
    @PreAuthorize("hasPermission(#courseId, 'VIEWER')")
    @CourseOpenStatus
    @Operation(summary = "Отримати список всіх конференцій у курсі.", description = "Список конференцій, який не містить деталей її учасників. Потребує ролі не нижче VIEWER. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Список всіх конференцій успішно повернено.", content = [Content(schema = Schema(implementation = List::class))])
    @ApiResponse(responseCode = "404", description = "Конференцію не знайдено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun getConferences(@PathVariable courseId: Long): ResponseEntity<Any> {
        return try {
            val conferences = conferenceService.getCourseConferencesDTOs(courseId)

            ResponseEntity.ok(conferences)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse("Conferences was not found: ${e.message}"))
        }
    }

    @GetMapping("/{conferenceId}")
    @PreAuthorize("hasPermission(#courseId, 'VIEWER')")
    @CourseOpenStatus
    @Operation(summary = "Отримати деталі конференції у курсі.", description = "Усі деталі конференції. Потребує ролі не нижче VIEWER. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Деталі конференції успішно повернено.", content = [Content(schema = Schema(implementation = ConferenceDTO::class))])
    @ApiResponse(responseCode = "404", description = "Конференцію не знайдено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun getConference(@PathVariable courseId: Long, @PathVariable conferenceId: Long): ResponseEntity<Any> {
        return try {
            val conferenceDto = conferenceService.getConferenceDTO(conferenceId)

            ResponseEntity.ok(conferenceDto)
        } catch(e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse("Conference was not found: ${e.message}"))
        }
    }

}