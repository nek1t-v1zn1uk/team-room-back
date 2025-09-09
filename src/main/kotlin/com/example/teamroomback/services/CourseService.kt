package com.example.teamroomback.services

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMember
import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.repositories.CourseMemberRepository
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.server.MethodNotAllowedException
import javax.management.InstanceNotFoundException

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val courseMemberRepository: CourseMemberRepository,
    private val userRepository: UserRepository,
    private val profileService: ProfileService
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

    fun getCourseById(courseId: Long): Course {
        return courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
    }

    fun addCourseMember(username: String, courseId: Long, request: AddCourseMemberRequest): CourseMember {
        // no more than one owner
        if(request.role == CourseMemberRole.OWNER)
            throw IllegalArgumentException("You dont have permission to add members with role \"${request.role}\"")

        // user(who is being added) must exist
        val user = userRepository.findByUsernameValue(request.username)
            ?: throw InstanceNotFoundException("User with username \"${request.username}\" not found")

        // user(who is being added) must have profile
        if(!profileService.hasProfile(request.username))
            throw InstanceNotFoundException("Profile for user with username \"${request.username}\" not found")

        val currentMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)
            ?: throw InstanceNotFoundException("User with username \"$username\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to add user
        if(!currentMember.role.canManage(request.role))
            throw IllegalAccessException("You dont have permission to add members with role \"${request.role}\"")

        // user must not be a member of course
        if(courseMemberRepository.findByUserUsernameValueAndCourseId(request.username, courseId) != null)
            throw IllegalArgumentException("User with username \"${request.username}\" is already a member of course with id \"${courseId}\"")


        val courseMember = courseMemberRepository.save(CourseMember(
            user = user,
            course = courseRepository.findCourseById(courseId)
                ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found"),
            role = request.role,
        ))
        return courseMember
    }

    fun deleteCourse(courseId: Long) {
        courseRepository.deleteById(courseId)
    }

    fun getUserCourses(username: String): List<Course> {
        val courses = courseRepository.findCoursesByUsername(username)
        return courses
    }



}