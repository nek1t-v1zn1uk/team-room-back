package com.example.teamroomback.security

import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.services.CourseService
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class CustomPermissionEvaluator(
    private val courseService: CourseService,
) : PermissionEvaluator {

    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any?): Boolean {

        if (permission is String) {

            val courseId = targetDomainObject as Long
            val requiredRole = CourseMemberRole.valueOf(permission)

            val userRole = courseService.getRoleInCourse(authentication.name, courseId)
                ?: throw NoSuchElementException(
                    "Course membership for user \"${authentication.name}\" in course with id \"${courseId}\" not found")

            return userRole.isAtLeast(requiredRole)
        }
        return false
    }

    // not using that method
    override fun hasPermission(authentication: Authentication, targetId: Serializable?, targetType: String?, permission: Any?): Boolean {
        // using another method, so always return false
        return false
    }
}