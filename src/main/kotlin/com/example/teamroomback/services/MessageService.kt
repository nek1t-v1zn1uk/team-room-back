package com.example.teamroomback.services

import com.example.teamroomback.repositories.MessageRepository
import org.springframework.stereotype.Service

@Service
class MessageService(
    val messageRepository: MessageRepository
) {
}