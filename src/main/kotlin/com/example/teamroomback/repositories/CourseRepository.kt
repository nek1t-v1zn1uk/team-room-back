package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<Course, Long> {
    fun findCourseById(id: Long): Course?

    @Query(value = "SELECT course FROM Course course " +
            "join CourseMember member on course.id = member.course.id " +
            "where member.user.usernameValue = :username")
    fun findCoursesByUsername(username: String): List<Course>
}