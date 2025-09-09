package com.example.teamroomback.utils

import com.example.teamroomback.services.CourseService
import com.example.teamroomback.validation.CourseOpenStatus
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Aspect
@Component
class CourseStatusAspect(
    private val courseService: CourseService
) {

    @Before("@annotation(courseOpenStatus) && args(id, ..)")
    fun checkCourseStatus(joinPoint: JoinPoint, id: Long, courseOpenStatus: CourseOpenStatus) {
        println("Is starts")
        if (courseService.getCourseById(id).isOpen != courseOpenStatus.requiredOpenStatus) {
            throw AccessDeniedException("Course must " +
                    (if(courseOpenStatus.requiredOpenStatus)"" else "not ") +
                    "be open")
        }
    }
}