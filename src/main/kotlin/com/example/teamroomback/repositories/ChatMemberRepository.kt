package com.example.teamroomback.repositories

import com.example.teamroomback.entities.ChatMember
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ChatMemberRepository : JpaRepository<ChatMember, Long> {
    fun findByChatIdAndUserUsername(chatId: Long, username: String): Optional<ChatMember>

    fun existsByChatIdAndUserUsername(chatId: Long, username: String): Boolean

    fun findAllByChatId(chatId: Long): List<ChatMember>

    fun deleteByChatIdAndUserUsername(chatId: Long, username: String)
}