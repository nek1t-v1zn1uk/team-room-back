package com.example.teamroomback.entities

import jakarta.persistence.*

@Entity
@Table(name = "assignment_media")
data class AssignmentMedia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    var assignment: Assignment,

    @Column(name = "name", length = 255)
    var name: String? = null,

    @Column(name = "file_url", columnDefinition = "text")
    var fileUrl: String? = null
)