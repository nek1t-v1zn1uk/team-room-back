package com.example.teamroomback.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "message_reactions", uniqueConstraints = [
    UniqueConstraint(name = "msg_user_reaction_key", columnNames = ["message_id", "user_id", "reaction_emoji"])
])
data class MessageReaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: ChatMessage,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "reaction_emoji", length = 10, nullable = false)
    var reactionEmoji: String,

    @CreationTimestamp
    @Column(name = "reaction_time", nullable = false, updatable = false)
    val reactionTime: LocalDateTime? = null
)