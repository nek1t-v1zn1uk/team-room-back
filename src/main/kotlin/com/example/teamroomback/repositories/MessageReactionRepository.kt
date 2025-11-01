package com.example.teamroomback.repositories

import com.example.teamroomback.entities.MessageReaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageReactionRepository: JpaRepository<MessageReaction, Long> {
    fun findByMessageIdAndUserUsernameValue(messageId: Long, username: String): MessageReaction?
}