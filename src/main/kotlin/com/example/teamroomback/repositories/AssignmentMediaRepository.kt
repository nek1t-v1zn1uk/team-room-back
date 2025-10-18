package com.example.teamroomback.repositories

import com.example.teamroomback.entities.AssignmentMedia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssignmentMediaRepository : JpaRepository<AssignmentMedia, Long>