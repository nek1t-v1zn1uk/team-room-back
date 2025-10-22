package com.example.teamroomback.services

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.dtos.PatchCourseRequest
import com.example.teamroomback.dtos.PutCourseMemberRoleRequest
import com.example.teamroomback.dtos.PutCourseRequest
import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMember
import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.repositories.CourseMemberRepository
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.UserRepository
import org.springframework.stereotype.Service
import javax.management.InstanceNotFoundException

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val courseMemberRepository: CourseMemberRepository,
    private val userRepository: UserRepository,
    private val profileService: ProfileService,
    private val webSocketNotificationService: WebSocketNotificationService
) {

    fun getRoleInCourse(username: String, courseId: Long): CourseMemberRole? {
        return courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)?.role
    }

    fun createCourse(username: String, request: CreateCourseRequest): Course {
        val course = courseRepository.save(
            Course(
                name = request.name,
                photoUrl = request.photoUrl
            )
        )

        val user = userRepository.findByUsernameValue(username)
        val courseMember = courseMemberRepository.save(
            CourseMember(
                user = user!!,
                course = course,
                role = CourseMemberRole.OWNER,
            )
        )

        return course
    }

    fun getCourseById(courseId: Long): Course {
        return courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
    }

    fun putCourse(courseId: Long, request: PutCourseRequest): Course {
        val course = getCourseById(courseId)
        course.name = request.name
        course.photoUrl = request.photoUrl

        return courseRepository.save(course)
    }

    fun patchCourse(courseId: Long, request: PatchCourseRequest): Course {
        val course = getCourseById(courseId)
        request.name?.let { course.name = it }
        request.photoUrl?.let { course.photoUrl = it }

        return courseRepository.save(course)
    }

    fun openCourse(courseId: Long) {
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
        course.isOpen = true
        courseRepository.save(course)
    }

    fun closeCourse(courseId: Long) {
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
        course.isOpen = false
        courseRepository.save(course)
    }

    fun deleteCourse(courseId: Long) {
        val course = courseRepository.findCourseById(courseId)

        courseRepository.deleteById(courseId)

        if(course != null) {
            for(member in course.courseMembers) {
                webSocketNotificationService.notifyUserAboutCourseDeletion(member)
            }
        }
    }

    fun getUserCourses(username: String): List<Course> {
        val courses = courseRepository.findCoursesByUsername(username)
        return courses
    }

    fun addCourseMember(username: String, courseId: Long, request: AddCourseMemberRequest): CourseMember {
        // no more than one owner
        if (request.role == CourseMemberRole.OWNER)
            throw IllegalArgumentException("You dont have permission to add members with role \"${request.role}\"")

        // user(who is being added) must exist
        val user = userRepository.findByUsernameValue(request.username)
            ?: throw InstanceNotFoundException("User with username \"${request.username}\" not found")

        // user(who is being added) must have profile
        if (!profileService.hasProfile(request.username))
            throw InstanceNotFoundException("Profile for user with username \"${request.username}\" not found")

        val currentMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)
            ?: throw InstanceNotFoundException("User with username \"$username\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to add user
        if (!currentMember.role.canManage(request.role))
            throw IllegalAccessException("You dont have permission to add members with role \"${request.role}\"")

        // user(who is being added) must not be a member of course
        if (courseMemberRepository.findByUserUsernameValueAndCourseId(request.username, courseId) != null)
            throw IllegalArgumentException("User with username \"${request.username}\" is already a member of course with id \"${courseId}\"")


        val courseMember = courseMemberRepository.save(
            CourseMember(
                user = user,
                course = courseRepository.findCourseById(courseId)
                    ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found"),
                role = request.role,
            )
        )

        webSocketNotificationService.notifyUserAboutJoiningToCourse(courseMember)

        return courseMember
    }

    fun changeCourseMemberRole(username: String, courseId: Long, request: PutCourseMemberRoleRequest): CourseMember {
        // no more than one owner
        if (request.role == CourseMemberRole.OWNER)
            throw IllegalArgumentException("You dont have permission to set role \"${request.role}\"")

        val currentMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)
            ?: throw InstanceNotFoundException("User with username \"$username\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to change member`s role
        if (!currentMember.role.canManage(request.role))
            throw IllegalAccessException("You dont have permission to set member`s role to \"${request.role}\"")

        val changingCourseMember = courseMemberRepository.findByUserUsernameValueAndCourseId(request.username, courseId)
            ?: throw IllegalArgumentException("User with username \"${request.username}\" must be a member of course with id \"${courseId}\"")

        // user must have enough rights to change member with role
        if (!currentMember.role.canManage(changingCourseMember.role))
            throw IllegalAccessException("You dont have permission to change member with role \"${request.role}\"")

        val oldRole = changingCourseMember.role
        changingCourseMember.role = request.role
        val newMember = courseMemberRepository.save(changingCourseMember)
        webSocketNotificationService.notifyUserAboutRoleChangeInCourse(newMember, oldRole.name)
        return newMember
    }

    fun deleteCourseMember(adminUsername: String, courseId: Long, memberUsername: String) {
        val adminMember = courseMemberRepository.findByUserUsernameValueAndCourseId(adminUsername, courseId)
            ?: throw InstanceNotFoundException("User with username \"$adminUsername\" is not a member of course with id \"${courseId}\"")
        val member =  courseMemberRepository.findByUserUsernameValueAndCourseId(memberUsername, courseId)
        ?: throw InstanceNotFoundException("User with username \"$memberUsername\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to delete member
        if (!adminMember.role.canManage(member.role))
            throw IllegalAccessException("You dont have permission to delete member with role \"${member.role}\"")

        courseMemberRepository.delete(member)

        webSocketNotificationService.notifyUserAboutRemovalFromCourse(member)
    }

}