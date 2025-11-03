package com.example.teamroomback.repositories

import com.example.teamroomback.entities.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findTopByChatIdOrderByIdDesc(chatId: Long): ChatMessage?
}