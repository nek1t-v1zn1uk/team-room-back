package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.AddCourseMemberResponse
import com.example.teamroomback.dtos.CourseDTO
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.dtos.CreateCourseResponse
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.UserCoursesResponse
import com.example.teamroomback.services.CourseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
            ))
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Course creation failed: ${e.message}.",
                )
            )
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    fun updateCourse(@PathVariable id: Long): String {
        return "GG"
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'OWNER')")
    fun deleteCourse(@PathVariable id: Long): String {
        return "GG"
    }


    fun openCourse(){

    }

    fun closeCourse(){

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