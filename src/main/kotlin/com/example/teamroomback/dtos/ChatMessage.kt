package com.example.teamroomback.dtos

import java.time.Instant

data class ChatMessage(
    val sender: String,
    val content: String? = null,
    //val timestamp: Instant = Instant.now(),
    val type: MessageType,
)
enum class MessageType {
    CHAT,
    JOIN,
    LEAVE
}