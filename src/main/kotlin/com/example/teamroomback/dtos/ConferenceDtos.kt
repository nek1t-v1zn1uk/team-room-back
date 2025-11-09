package com.example.teamroomback.dtos

import com.example.teamroomback.entities.ConferenceParticipantRole
import com.example.teamroomback.entities.ConferenceStatus
import java.time.LocalDateTime


data class ConferenceDTO(
    val id: Long,
    val courseId: Long,
    var subject: String? = null,
    var roomName: String,
    var status: ConferenceStatus,
    val createdAt: LocalDateTime,
    var endedAt: LocalDateTime? = null,
    var participants: List<ShortConferenceParticipantDTO> = listOf()
)

data class ShortConferenceDTO(
    val id: Long,
    val courseId: Long,
    var subject: String? = null,
    var roomName: String,
    var status: ConferenceStatus,
    val createdAt: LocalDateTime,
    var endedAt: LocalDateTime? = null
)

data class ShortConferenceParticipantDTO(
    val username: String,
    var role: ConferenceParticipantRole,
    val joinedAt: LocalDateTime,
    var leftAt: LocalDateTime? = null
)

data class CreateConferenceRequest(
    val subject: String
)

data class ConferenceJoinDetails(
    val jwt: String,
    val roomName: String,
    val role: ConferenceParticipantRole,
)