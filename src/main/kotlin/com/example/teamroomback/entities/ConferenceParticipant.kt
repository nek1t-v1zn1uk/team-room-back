package com.example.teamroomback.entities

import com.example.teamroomback.dtos.ShortConferenceParticipantDTO
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "conference_participants")
class ConferenceParticipant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    val conference: Conference,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "conference_participant_role")
    var role: ConferenceParticipantRole = ConferenceParticipantRole.MEMBER,

    @Column(name = "joined_at", nullable = false, updatable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "left_at")
    var leftAt: LocalDateTime? = null
) {
    fun toShortConferenceParticipantDTO() = ShortConferenceParticipantDTO(
        user.username,
        role,
        joinedAt,
        leftAt
    )
}

enum class ConferenceParticipantRole {
    MODERATOR,
    MEMBER,
    VIEWER
}