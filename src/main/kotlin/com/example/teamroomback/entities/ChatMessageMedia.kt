package com.example.teamroomback.entities

import jakarta.persistence.*

@Entity
@Table(name = "chat_message_media")
data class ChatMessageMedia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: ChatMessage,

    @Column(name = "file_url", nullable = false, columnDefinition = "text")
    val fileUrl: String,

    @Column(name = "file_name", length = 255)
    var fileName: String? = null,

    @Column(name = "file_type", length = 100)
    var fileType: String? = null,

    @Column(name = "file_size_bytes")
    var fileSizeBytes: Int? = null
)