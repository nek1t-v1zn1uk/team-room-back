package com.example.teamroomback.repositories

import com.example.teamroomback.entities.AssignmentResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssignmentResponseRepository : JpaRepository<AssignmentResponse, Long>