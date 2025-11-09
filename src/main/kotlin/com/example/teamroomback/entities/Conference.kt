package com.example.teamroomback.entities

import com.example.teamroomback.dtos.ConferenceDTO
import com.example.teamroomback.dtos.ShortConferenceDTO
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "conferences")
data class Conference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course,

    @Column(columnDefinition = "TEXT")
    var subject: String? = null,

    @Column(name = "room_name", nullable = false, unique = true)
    var roomName: String = "course-${course.id}-conf-${UUID.randomUUID()}",

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "conference_status")
    var status: ConferenceStatus = ConferenceStatus.ACTIVE,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ended_at")
    var endedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "conference", cascade = [CascadeType.ALL], orphanRemoval = true)
    var participants: MutableList<ConferenceParticipant> = mutableListOf()
) {
    fun toConferenceDTO() = ConferenceDTO(
        id!!,
        course.id!!,
        subject,
        roomName,
        status,
        createdAt,
        endedAt,
        participants.map { it.toShortConferenceParticipantDTO() }
    )
    fun toShortConferenceDTO() = ShortConferenceDTO(
        id!!,
        course.id!!,
        subject,
        roomName,
        status,
        createdAt,
        endedAt,
    )
}

enum class ConferenceStatus {
    ACTIVE,
    ENDED
}