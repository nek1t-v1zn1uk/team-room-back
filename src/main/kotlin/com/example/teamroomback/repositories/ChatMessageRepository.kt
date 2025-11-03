package com.example.teamroomback.repositories

import com.example.teamroomback.entities.ChatMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findTopByChatIdOrderByIdDesc(chatId: Long): ChatMessage?

    @Query("SELECT m FROM ChatMessage m WHERE m.chat.id = :chatId AND m.id < :messageId ORDER BY m.id DESC")
    fun findMessagesBefore(chatId: Long, messageId: Long, pageable: Pageable): List<ChatMessage>
    @Query("SELECT m FROM ChatMessage m WHERE m.chat.id = :chatId AND m.id > :messageId ORDER BY m.id ASC")
    fun findMessagesAfter(chatId: Long, messageId: Long, pageable: Pageable): List<ChatMessage>
}