package com.example.teamroomback.repositories

import com.example.teamroomback.entities.AssignmentResponseMedia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssignmentResponseMediaRepository : JpaRepository<AssignmentResponseMedia, Long>