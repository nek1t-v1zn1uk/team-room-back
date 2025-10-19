package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.*
import com.example.teamroomback.services.AssignmentService
import com.example.teamroomback.validation.CourseOpenStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
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
    @Operation(summary = "Створити нове завдання у курсі.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання - керування завданнями курсу"])
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
    @Operation(summary = "Отримати всі завдання курсу.", description = "Потребує ролі не нижче VIEWER.", tags=["Завдання - керування завданнями курсу"])
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
    @Operation(summary = "Отримати завдання за ID.", description = "Потребує ролі не нижче VIEWER.", tags=["Завдання - керування завданнями курсу"])
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
    @Operation(summary = "Повністю оновити завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання - керування завданнями курсу"])
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
    @Operation(summary = "Частково оновити завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання - керування завданнями курсу"])
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
    @Operation(summary = "Видалити завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання - керування завданнями курсу"])
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
    @Operation(summary = "Додати медіафайл до завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, медіа - керування медіа в завданнях курсу"])
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
    @Operation(summary = "Перейменувати медіафайл.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, медіа - керування медіа в завданнях курсу"])
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
    @Operation(summary = "Видалити медіафайл.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, медіа - керування медіа в завданнях курсу"])
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


    @PostMapping("/{assignmentId}/responses")
    @PreAuthorize("hasPermission(#courseId, 'STUDENT')")
    @CourseOpenStatus
    @Operation(summary = "Надіслати відповідь на завдання.", description = "Потребує ролі не нижче STUDENT. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "201", description = "Відповідь успішно надіслано.", content = [Content(schema = Schema(implementation = CreateAssignmentResponseResponse::class))])
    fun createAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Valid @RequestBody request: CreateAssignmentResponseRequest
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val response = assignmentService.createAssignmentResponse(username, assignmentId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(
                CreateAssignmentResponseResponse(
                    id = response.id!!,
                    message = "Assignment response created successfully"
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @GetMapping("/{assignmentId}/responses")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Отримати всі відповіді на завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Список відповідей успішно отримано.")
    fun getAssignmentResponses(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long
    ): ResponseEntity<Any> {
        return try {
            val responses = assignmentService.getAssignmentResponses(assignmentId)
            val responseDTOs = responses.map { it.toAssignmentResponseShortDTO() }
            ResponseEntity.ok(mapOf("responses" to responseDTOs))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @GetMapping("/{assignmentId}/responses/{responseId}")
    @PreAuthorize("hasPermission(#courseId, 'STUDENT')") // Professor can also view, STUDENT is minimum
    @CourseOpenStatus
    @Operation(summary = "Отримати відповідь на завдання за ID.", description = "Потребує ролі не нижче STUDENT. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Відповідь успішно отримано.", content = [Content(schema = Schema(implementation = AssignmentResponseDTO::class))])
    fun getAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID відповіді") @PathVariable responseId: Long
    ): ResponseEntity<Any> {
        return try {
            val response = assignmentService.getAssignmentResponse(responseId)
            ResponseEntity.ok(response.toAssignmentResponseDTO())
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @DeleteMapping("/{assignmentId}/responses/{responseId}")
    @PreAuthorize("hasPermission(#courseId, 'STUDENT')")
    @CourseOpenStatus
    @Operation(summary = "Видалити відповідь на завдання.", description = "Доступно тільки автору відповіді, якщо вона ще не оцінена. Потребує ролі не нижче STUDENT. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Відповідь успішно видалено.")
    fun deleteAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID відповіді") @PathVariable responseId: Long
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            assignmentService.deleteAssignmentResponse(responseId, username)
            ResponseEntity.ok(SimpleMessageResponse("Assignment response deleted successfully"))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message.toString()))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PostMapping("/{assignmentId}/responses/{responseId}/grade")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Оцінити відповідь на завдання.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Відповідь успішно оцінено.")
    fun gradeAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID відповіді") @PathVariable responseId: Long,
        @Valid @RequestBody request: GradeAssignmentResponseRequest
    ): ResponseEntity<Any> {
        return try {
            assignmentService.gradeAssignmentResponse(responseId, request)
            ResponseEntity.ok(SimpleMessageResponse("Assignment response graded successfully"))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PostMapping("/{assignmentId}/responses/{responseId}/return")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Повернути відповідь на доопрацювання.", description = "Неможливо повернути вже оцінену роботу. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Відповідь успішно повернуто.")
    fun returnAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID відповіді") @PathVariable responseId: Long,
        @Valid @RequestBody request: ReturnAssignmentResponseRequest
    ): ResponseEntity<Any> {
        return try {
            assignmentService.returnAssignmentResponse(responseId, request)
            ResponseEntity.ok(SimpleMessageResponse("Assignment response returned successfully"))
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message.toString()))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(SimpleMessageResponse(e.message.toString()))
        }
    }

    @PostMapping("/{assignmentId}/responses/{responseId}/grade-cancel")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Скасувати оцінку відповіді.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Оцінку успішно скасовано.")
    fun cancelGradeAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID відповіді") @PathVariable responseId: Long
    ): ResponseEntity<Any> {
        assignmentService.cancelGradeAssignmentResponse(responseId)
        return ResponseEntity.ok(SimpleMessageResponse("Grade canceled successfully"))
    }

    @PostMapping("/{assignmentId}/responses/{responseId}/return-cancel")
    @PreAuthorize("hasPermission(#courseId, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Скасувати повернення відповіді.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.", tags=["Завдання, відповіді - керування відповідями в завданнях курсу"])
    @ApiResponse(responseCode = "200", description = "Повернення успішно скасовано.")
    fun returnCancelAssignmentResponse(
        @Parameter(description = "ID курсу") @PathVariable courseId: Long,
        @Parameter(description = "ID завдання") @PathVariable assignmentId: Long,
        @Parameter(description = "ID відповіді") @PathVariable responseId: Long
    ): ResponseEntity<Any> {
        assignmentService.cancelReturnAssignmentResponse(responseId)
        return ResponseEntity.ok(SimpleMessageResponse("Return canceled successfully"))
    }
}