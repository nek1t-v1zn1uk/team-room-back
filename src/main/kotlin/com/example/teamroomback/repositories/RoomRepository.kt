package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository: JpaRepository<Room, Long> {
    fun findRoomById(id: Long): Room?
    @Query("select room from Room room join RoomMember member on room.id = member.room.id where member.user.id = :userId")
    fun findRoomsByUsername(userId: Long): List<Room>

}