package com.example.teamroomback.repositories

import com.example.teamroomback.entities.CourseMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseMemberRepository : JpaRepository<CourseMember, Long> {
    fun findByUserUsernameValueAndCourseId(usernameValue: String, courseId: Long): CourseMember?
}