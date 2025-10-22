package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.AddCourseMemberResponse
import com.example.teamroomback.dtos.CourseDTO
import com.example.teamroomback.dtos.CourseMemberDTO
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.dtos.CreateCourseResponse
import com.example.teamroomback.dtos.DeleteCourseMemberResponse
import com.example.teamroomback.dtos.DeleteCourseResponse
import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.PatchCourseRequest
import com.example.teamroomback.dtos.PatchCourseResponse
import com.example.teamroomback.dtos.PutCourseMemberRoleRequest
import com.example.teamroomback.dtos.PutCourseMemberRoleResponse
import com.example.teamroomback.dtos.PutCourseRequest
import com.example.teamroomback.dtos.PutCourseResponse
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.UserCoursesResponse
import com.example.teamroomback.services.CourseService
import com.example.teamroomback.services.WebSocketNotificationService
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
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.management.InstanceNotFoundException

@RestController
@RequestMapping("/api/course")
@Tag(name = "Курси", description = "Ендпоїнти для керування курсами та їх учасниками.")
@SecurityRequirement(name = "bearerAuth")
class CourseController(
    private val courseService: CourseService,
) {

    @PostMapping
    @Operation(summary = "Створити новий курс.", description = "Створює новий курс і автоматично робить поточного користувача його власником (OWNER).")
    @ApiResponse(responseCode = "200", description = "Курс успішно створено.", content = [Content(schema = Schema(implementation = CreateCourseResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації даних.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun createCourse(@Valid @RequestBody request: CreateCourseRequest): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication

            val course = courseService.createCourse(authentication.name, request)

            ResponseEntity.ok(CreateCourseResponse(
                courseId = course.id!!,
                message = "Course created successfully"
            ))
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course creation failed: ${e.message}.",
                )
            )
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'VIEWER')")
    @Operation(summary = "Отримати інформацію про курс.", description = "Повертає деталі курсу, включаючи список учасників. Потребує ролі не нижче VIEWER.")
    @ApiResponse(responseCode = "200", description = "Дані курсу успішно отримано.", content = [Content(schema = Schema(implementation = CourseDTO::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено (недостатньо прав).", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getCourse(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long): ResponseEntity<Any> {
        return try {

            val course = courseService.getCourseById(id)

            ResponseEntity.ok(
                CourseDTO(
                    id = course.id!!,
                    name= course.name,
                    photoUrl = course.photoUrl,
                    isOpen = course.isOpen,
                    members = course.courseMembers.map {
                        CourseMemberDTO(
                            username = it.user.username,
                            role = it.role,
                            createdAt = it.createdAt
                        )
                    }
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course searching and sharing failed: ${e.message}.",
                )
            )
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @Operation(summary = "Повністю оновити курс.", description = "Замінює дані курсу на нові. Потребує ролі не нижче PROFESSOR.")
    @ApiResponse(responseCode = "200", description = "Курс успішно оновлено.", content = [Content(schema = Schema(implementation = PutCourseResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @CourseOpenStatus
    fun putCourse(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long, @Valid @RequestBody request: PutCourseRequest): ResponseEntity<Any> {
        return try {

            val newCourse = courseService.putCourse(id, request)

            ResponseEntity.ok(
                PutCourseResponse(
                    courseId = newCourse.id!!,
                    message = "Course put successfully"
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course putting failed: ${e.message}.",
                )
            )
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @Operation(summary = "Частково оновити курс.", description = "Оновлює лише передані поля курсу. Потребує ролі не нижче PROFESSOR.")
    @ApiResponse(responseCode = "200", description = "Курс успішно оновлено.", content = [Content(schema = Schema(implementation = PatchCourseResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @CourseOpenStatus
    fun patchCourse(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long, @Valid @RequestBody request: PatchCourseRequest): ResponseEntity<Any> {
        return try {

            val newCourse = courseService.patchCourse(id, request)

            ResponseEntity.ok(
                PatchCourseResponse(
                    courseId = newCourse.id!!,
                    message = "Course patched successfully"
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course patching failed: ${e.message}.",
                )
            )
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'OWNER')")
    @Operation(summary = "Видалити курс.", description = "Повністю видаляє курс та всі пов'язані з ним дані. Потребує ролі OWNER.")
    @ApiResponse(responseCode = "200", description = "Курс успішно видалено.", content = [Content(schema = Schema(implementation = DeleteCourseResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteCourse(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long): ResponseEntity<Any> {
        return try {

            courseService.deleteCourse(id)

            ResponseEntity.ok(
                DeleteCourseResponse(
                    courseId = id,
                    message = "Course deleted successfully"
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course deletion failed: ${e.message}.",
                )
            )
        }
    }

    @PostMapping("/{id}/open")
    @PreAuthorize("hasPermission(#id, 'OWNER')")
    @CourseOpenStatus(false)
    @Operation(summary = "Відкрити курс.", description = "Робить курс відкритим для приєднання нових учасників. Потребує ролі OWNER. Курс повинний бути закритим.")
    @ApiResponse(responseCode = "200", description = "Курс успішно відкрито.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "409", description = "Конфлікт. Курс вже відкрито.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun openCourse(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long): ResponseEntity<Any> {
        return try{
            courseService.openCourse(id)

            ResponseEntity.ok(
                SimpleMessageResponse(
                    message = "Course opened successfully",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course opening failed: ${e.message}.",
                )
            )
        }
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasPermission(#id, 'OWNER')")
    @CourseOpenStatus
    @Operation(summary = "Закрити курс.", description = "Робить курс закритим, забороняючи приєднання нових учасників. Потребує ролі OWNER. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Курс успішно закрито.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "409", description = "Конфлікт. Курс вже закрито.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun closeCourse(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long): ResponseEntity<Any> {
        return try{
            courseService.closeCourse(id)

            ResponseEntity.ok(
                SimpleMessageResponse(
                    message = "Course closed successfully",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course closing failed: ${e.message}.",
                )
            )
        }
    }

    @GetMapping
    @Operation(summary = "Отримати курси поточного користувача.", description = "Повертає список всіх курсів, до яких належить аутентифікований користувач.")
    @ApiResponse(responseCode = "200", description = "Список курсів отримано.", content = [Content(schema = Schema(implementation = UserCoursesResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun findAllCourses(): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication

            val courses = courseService.getUserCourses(authentication.name)
                .map{ CourseDTO(id = it.id!!, name = it.name, photoUrl = it.photoUrl, isOpen = it.isOpen) }

            ResponseEntity.ok(
                UserCoursesResponse(
                    username = authentication.name,
                    courses = courses
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course searching and sharing failed: ${e.message}.",
                )
            )
        }
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasPermission(#id, 'LEADER')")
    @CourseOpenStatus
    @Operation(summary = "Додати учасника до курсу.", description = "Додає користувача до курсу з вказаною роллю. Потребує ролі не нижче LEADER. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Учасника успішно додано.", content = [Content(schema = Schema(implementation = AddCourseMemberResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації (користувач вже в курсі / не існує).", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun addMember(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long, @Valid @RequestBody request: AddCourseMemberRequest): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication

            val courseMember = courseService.addCourseMember(authentication.name, id, request)

            ResponseEntity.ok(
                AddCourseMemberResponse(
                    message = "Member joined successfully",
                    username = courseMember.user.username,
                    courseId = courseMember.course.id!!,
                )
            )
        } catch (e: IllegalArgumentException){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Member joining failed: ${e.message}.",
                )
            )
        } catch (e: InstanceNotFoundException){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Member joining failed: ${e.message}.",
                )
            )
        } catch (e: IllegalAccessException){
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                SimpleMessageResponse(
                    message = "Member joining failed: ${e.message}.",
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Member joining failed: ${e.message}.",
                )
            )
        }
    }

    @PutMapping("/{id}/members")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Змінити роль учасника.", description = "Оновлює роль існуючого учасника курсу. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Роль учасника змінено.", content = [Content(schema = Schema(implementation = PutCourseMemberRoleResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка (користувач не є учасником курсу).", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun changeMemberRole(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long, @Valid @RequestBody request: PutCourseMemberRoleRequest): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication

            val courseMember = courseService.changeCourseMemberRole(authentication.name, id, request)

            ResponseEntity.ok(
                PutCourseMemberRoleResponse(
                    username = courseMember.user.username,
                    newRole = courseMember.role,
                    message = "Member role changed successfully",
                )
            )
        } catch (e: IllegalArgumentException){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Member role changing failed: ${e.message}.",
                )
            )
        } catch (e: InstanceNotFoundException){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Member role changing failed: ${e.message}.",
                )
            )
        } catch (e: IllegalAccessException){
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                SimpleMessageResponse(
                    message = "Member role changing failed: ${e.message}.",
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Member role changing failed: ${e.message}.",
                )
            )
        }
    }

    @DeleteMapping("/{id}/members")
    @PreAuthorize("hasPermission(#id, 'LEADER')")
    @CourseOpenStatus
    @Operation(summary = "Видалити учасника з курсу.", description = "Видаляє вказаного користувача зі списку учасників курсу. Потребує ролі не нижче LEADER. Курс повиннйи бути відкритим")
    @ApiResponse(responseCode = "200", description = "Учасника успішно видалено.", content = [Content(schema = Schema(implementation = DeleteCourseMemberResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка (користувач не є учасником курсу).", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteMember(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long, @Parameter(description = "Логін користувача, якого потрібно видалити.", example = "student123") @RequestParam username: String): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication

            courseService.deleteCourseMember(authentication.name, id, username)

            ResponseEntity.ok(
                DeleteCourseMemberResponse(
                    username = username,
                    message = "Member deleted successfully",
                )
            )
        } catch (e: InstanceNotFoundException){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Member deletion failed: ${e.message}.",
                )
            )
        } catch (e: IllegalAccessException){
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                SimpleMessageResponse(
                    message = "Member deletion failed: ${e.message}.",
                )
            )
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Member deletion failed: ${e.message}.",
                )
            )
        }
    }

}