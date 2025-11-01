package com.example.teamroomback.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "chats")
data class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", length = 255)
    var name: String? = null,

    @Column(name = "photo_url", columnDefinition = "text")
    var photoUrl: String? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "chat_type")
    val type: ChatType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    val course: Course? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: List<ChatMember> = listOf(),

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], orphanRemoval = true)
    val messages: List<ChatMessage> = listOf(),

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], orphanRemoval = true)
    val pinnedMessages: List<PinnedMessage> = listOf()
)

enum class ChatType {
    PRIVATE,
    GROUP,
    MAIN_COURSE_CHAT,
    COURSE_CHAT
}

enum class ChatMemberRole {
    OWNER,
    ADMIN,
    MODERATOR,
    MEMBER,
    VIEWER;

    fun isAtLeast(role: ChatMemberRole): Boolean {
        return this.ordinal <= role.ordinal
    }
}