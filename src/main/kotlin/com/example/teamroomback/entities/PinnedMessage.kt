package com.example.teamroomback.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "pinned_messages", uniqueConstraints = [
    UniqueConstraint(name = "chat_pinned_message_key", columnNames = ["chat_id", "message_id"])
])
data class PinnedMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: ChatMessage,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_by_user_id", nullable = false)
    val pinnedByUser: User,

    @CreationTimestamp
    @Column(name = "pinned_at", nullable = false, updatable = false)
    val pinnedAt: LocalDateTime? = null
)