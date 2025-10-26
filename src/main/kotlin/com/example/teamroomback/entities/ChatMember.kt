package com.example.teamroomback.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "chat_member", uniqueConstraints = [
    UniqueConstraint(name = "chat_member_chat_user_key", columnNames = ["chat_id", "user_id"])
])
data class ChatMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "chat_member_role")
    var role: ChatMemberRole = ChatMemberRole.MEMBER,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_accessible_message_id")
    var lastAccessibleMessage: ChatMessage? = null,

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    val joinedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    var lastReadMessage: ChatMessage? = null,

    @Column(name = "last_read_at")
    var lastReadAt: LocalDateTime? = null
)