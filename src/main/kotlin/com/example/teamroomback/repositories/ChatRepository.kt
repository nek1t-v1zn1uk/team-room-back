package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Chat
import com.example.teamroomback.entities.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository : JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.members m WHERE m.user.usernameValue = :username")
    fun findChatsByUsername(username: String): List<Chat>

    @Query("""
        SELECT c FROM Chat c JOIN c.members m1 JOIN c.members m2
        WHERE c.type = 'PRIVATE'
        AND m1.user.usernameValue = :firstUsername
        AND m2.user.usernameValue = :secondUsername
    """)
    fun findPrivateChatByMembersUsernames(firstUsername: String, secondUsername: String): Chat?
}