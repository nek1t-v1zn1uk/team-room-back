package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Assignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssignmentRepository : JpaRepository<Assignment, Long> {
    fun findByCourseId(courseId: Long): List<Assignment>
}