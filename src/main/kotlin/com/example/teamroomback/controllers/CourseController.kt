package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.AddCourseMemberResponse
import com.example.teamroomback.dtos.CourseDTO
import com.example.teamroomback.dtos.CourseMemberDTO
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.dtos.CreateCourseResponse
import com.example.teamroomback.dtos.DeleteCourseResponse
import com.example.teamroomback.dtos.PatchCourseRequest
import com.example.teamroomback.dtos.PatchCourseResponse
import com.example.teamroomback.dtos.PutCourseRequest
import com.example.teamroomback.dtos.PutCourseResponse
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.UserCoursesResponse
import com.example.teamroomback.services.CourseService
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
import org.springframework.web.bind.annotation.RestController
import javax.management.InstanceNotFoundException

@RestController
@RequestMapping("/api/course")
class CourseController(
    private val courseService: CourseService,
) {

    @PostMapping
    fun createCourse(@RequestBody request: CreateCourseRequest): ResponseEntity<Any> {
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
    fun getCourse(@PathVariable id: Long): ResponseEntity<Any> {
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
    fun putCourse(@PathVariable id: Long, @RequestBody request: PutCourseRequest): ResponseEntity<Any> {
        return try {

            val newCourse = courseService.putCourse(id, request)

            ResponseEntity.ok(
                PutCourseResponse(
                    courseId = newCourse.id!!,
                    message = "Course updated successfully"
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
    fun patchCourse(@PathVariable id: Long, @RequestBody request: PatchCourseRequest): ResponseEntity<Any> {
        return try {

            val newCourse = courseService.patchCourse(id, request)

            ResponseEntity.ok(
                PatchCourseResponse(
                    courseId = newCourse.id!!,
                    message = "Course updated successfully"
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
    fun deleteCourse(@PathVariable id: Long): ResponseEntity<Any> {
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
    fun openCourse(@PathVariable id: Long): ResponseEntity<Any> {
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
    fun closeCourse(@PathVariable id: Long): ResponseEntity<Any> {
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
    fun addMember(@RequestBody request: AddCourseMemberRequest, @PathVariable id: Long): ResponseEntity<Any> {
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


}