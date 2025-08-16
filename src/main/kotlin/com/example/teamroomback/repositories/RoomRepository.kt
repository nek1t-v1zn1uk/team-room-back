package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository: JpaRepository<Room, Long> {
    fun findRoomById(id: Long): Room?
}