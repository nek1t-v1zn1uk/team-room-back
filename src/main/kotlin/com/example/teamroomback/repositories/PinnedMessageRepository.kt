package com.example.teamroomback.repositories

import com.example.teamroomback.entities.PinnedMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PinnedMessageRepository: JpaRepository<PinnedMessage, Long> {
    fun findPinnedMessageByMessageId(messageId: Long): PinnedMessage?
    fun findAllByChatId(chatId: Long): List<PinnedMessage>

}