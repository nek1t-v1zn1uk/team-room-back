package com.example.teamroomback.services

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMember
import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.repositories.CourseMemberRepository
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import javax.management.InstanceNotFoundException

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val courseMemberRepository: CourseMemberRepository,
    private val userRepository: UserRepository,
) {

    fun getRoleInCourse(username: String, courseId: Long): CourseMemberRole? {
        return courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)?.role
    }

    fun createCourse(username: String, request: CreateCourseRequest): Course {
        val course = courseRepository.save(Course(
            name = request.name,
            photoUrl = request.photoUrl
        ))

        val user = userRepository.findByUsernameValue(username)
        val courseMember = courseMemberRepository.save(CourseMember(
            user = user!!,
            course = course,
            role = CourseMemberRole.OWNER,
        ))

        return course
    }

    fun addCourseMember(courseId: Long, request: AddCourseMemberRequest): CourseMember {
        val user = userRepository.findByUsernameValue(request.username)
            ?: throw UsernameNotFoundException("User with username \"${request.username}\" not found")
        val courseMember = courseMemberRepository.save(CourseMember(
            user = user,
            course = courseRepository.findCourseById(courseId)
                ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found"),
            role = request.role,
        ))
        return courseMember
    }

}