package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.AddAssignmentMediaRequest
import com.example.teamroomback.dtos.AssignmentDTO
import com.example.teamroomback.dtos.AssignmentMediaResponse
import com.example.teamroomback.dtos.CreateAssignmentRequest
import com.example.teamroomback.dtos.CreateAssignmentResponse
import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.PatchAssignmentRequest
import com.example.teamroomback.dtos.PutAssignmentRequest
import com.example.teamroomback.dtos.RenameAssignmentMediaRequest
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.services.AssignmentService
import com.example.teamroomback.validation.CourseOpenStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import javax.management.InstanceNotFoundException

@RestController
@RequestMapping("/api/course/{courseId}/assignments")
@Tag(name = "Завдання курсу", description = "Ендпоїнти для керування завданнями в межах курсу.")
@SecurityRequirement(name = "bearerAuth")
class AssignmentController(
    private val assignmentService: AssignmentService
) {

    @PostMapping
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Створити нове завдання у курсі.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "201", description = "Завдання успішно створено.", content = [Content(schema = Schema(implementation = CreateAssignmentResponse::class))])
    fun createAssignment(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Valid @RequestBody request: CreateAssignmentRequest
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val assignment = assignmentService.createAssignment(username, courseId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(
                CreateAssignmentResponse(
                    id = assignment.id!!,
                    message = "Assignment created successfully"
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @GetMapping
    @PreAuthorize("hasPermission(#courseId, 'VIEWER')")
    @Operation(summary = "Отримати всі завдання курсу.", description = "Потребує ролі не нижче VIEWER.")
    @ApiResponse(responseCode = "200", description = "Завдання курсу успішно отримано.")
    fun getCourseAssignments(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long
    ): ResponseEntity<Any> {
        return try {
            val assignments = assignmentService.getCourseAssignments(courseId)
            val assignmentDTOs = assignments.map { it.toAssignmentShortDTO() }
            ResponseEntity.ok(mapOf("assignments" to assignmentDTOs))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasPermission(#courseId, 'VIEWER')")
    @Operation(summary = "Отримати завдання за ID.", description = "Потребує ролі не нижче VIEWER.")
    @ApiResponse(responseCode = "200", description = "Завдання успішно отримано.", content = [Content(schema = Schema(implementation = AssignmentDTO::class))])
    fun getAssignment(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long
    ): ResponseEntity<Any> {
        return try {
            val assignment = assignmentService.getAssignment(assignmentId)
            ResponseEntity.ok(assignment.toAssignmentDTO())
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Повністю оновити завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Завдання успішно оновлено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun putAssignment(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Valid @RequestBody request: PutAssignmentRequest
    ): ResponseEntity<Any> {
        return try {
            assignmentService.putAssignment(assignmentId, request)
            ResponseEntity.ok(SimpleMessageResponse("Assignment updated successfully"))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PatchMapping("/{assignmentId}")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Частково оновити завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Завдання успішно оновлено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun patchAssignment(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Valid @RequestBody request: PatchAssignmentRequest
    ): ResponseEntity<Any> {
        return try {
            assignmentService.patchAssignment(assignmentId, request)
            ResponseEntity.ok(SimpleMessageResponse("Assignment patched successfully"))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Видалити завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Завдання успішно видалено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    fun deleteAssignment(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long
    ): ResponseEntity<Any> {
        return try {
            assignmentService.deleteAssignment(assignmentId)
            ResponseEntity.ok(SimpleMessageResponse("Assignment deleted successfully"))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PostMapping("/{assignmentId}/media")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Додати медіафайл до завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "201", description = "Медіафайл успішно додано.", content = [Content(schema = Schema(implementation = AssignmentMediaResponse::class))])
    fun addMedia(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Valid @RequestBody request: AddAssignmentMediaRequest
    ): ResponseEntity<Any> {
        return try {
            val media = assignmentService.addMedia(assignmentId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(
                AssignmentMediaResponse(
                    id = media.id!!,
                    assignmentId = assignmentId,
                    message = "Media added successfully"
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PatchMapping("/{assignmentId}/media/{mediaId}")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Перейменувати медіафайл.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Медіафайл успішно перейменовано.", content = [Content(schema = Schema(implementation = AssignmentMediaResponse::class))])
    fun renameMedia(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID медіафайлу") @PathVariable mediaId: Long,
        @Valid @RequestBody request: RenameAssignmentMediaRequest
    ): ResponseEntity<Any> {
        return try {
            val media = assignmentService.renameMedia(mediaId, request)
            ResponseEntity.ok(
                AssignmentMediaResponse(
                    id = media.id!!,
                    assignmentId = assignmentId,
                    message = "Media renamed successfully"
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @DeleteMapping("/{assignmentId}/media/{mediaId}")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Видалити медіафайл.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Медіафайл успішно видалено.", content = [Content(schema = Schema(implementation = AssignmentMediaResponse::class))])
    fun deleteMedia(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID медіафайлу") @PathVariable mediaId: Long
    ): ResponseEntity<Any> {
        return try {
            val media = assignmentService.deleteMedia(mediaId)
            ResponseEntity.ok(
                AssignmentMediaResponse(
                    id = media.id!!,
                    assignmentId = assignmentId,
                    message = "Media deleted successfully"
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }
}