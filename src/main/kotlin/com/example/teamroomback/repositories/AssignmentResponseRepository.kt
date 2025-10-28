package com.example.teamroomback.repositories

import com.example.teamroomback.entities.AssignmentResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AssignmentResponseRepository : JpaRepository<AssignmentResponse, Long> {
    fun findByAssignmentIdAndAuthorId(assignmentId: Long, authorId: Long): AssignmentResponse?
    fun findByAuthorIdAndAssignmentCourseId(authorId: Long, courseId: Long): List<AssignmentResponse>
}