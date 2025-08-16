package com.example.teamroomback.repositories

import com.example.teamroomback.entities.RoomMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<RoomMessage, Long> {

}