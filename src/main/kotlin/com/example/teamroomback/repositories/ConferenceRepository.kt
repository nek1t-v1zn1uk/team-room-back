package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Conference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConferenceRepository: JpaRepository<Conference, Long> {
    fun findAllByCourseId(courseId: Long): List<Conference>
    fun findByRoomName(roomName: String): Conference?
}