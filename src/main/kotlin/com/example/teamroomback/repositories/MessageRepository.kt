package com.example.teamroomback.repositories

import com.example.teamroomback.entities.RoomMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<RoomMessage, Long> {
    //fun findRoomMessagesByRoomId(roomId: Long): List<RoomMessage>
    @Query("select msg from RoomMessage msg " +
            "join fetch msg.sender " +
            "join fetch msg.room " +
            "where msg.room.id = :roomId " +
            "order by msg.sendAt")
    fun findRoomMessagesByRoomId(roomId: Long): List<RoomMessage>
}