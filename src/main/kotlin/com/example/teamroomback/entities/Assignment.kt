package com.example.teamroomback.entities

import com.example.teamroomback.dtos.AssignmentDTO
import com.example.teamroomback.dtos.AssignmentShortDTO
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "assignments")
data class Assignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    var course: Course,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @Column(name = "description", columnDefinition = "text")
    var description: String? = null,

    @Column(name = "max_grade")
    var maxGrade: Int = 100,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @Column(name = "deadline", nullable = false)
    var deadline: LocalDateTime,

    @OneToMany(mappedBy = "assignment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val media: List<AssignmentMedia> = listOf(),

    @OneToMany(mappedBy = "assignment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val responses: List<AssignmentResponse> = listOf()
) {
    fun toAssignmentDTO(): AssignmentDTO
    {
        return AssignmentDTO(
            id = this.id!!,
            title = this.title,
            description = this.description,
            maxGrade = this.maxGrade,
            createdAt = this.createdAt!!,
            deadline = this.deadline,
            authorUsername = this.author.username,
            media = this.media.map { it.toAssignmentMediaDTO() }
        )
    }

    fun toAssignmentShortDTO(): AssignmentShortDTO {
        return AssignmentShortDTO(
            id = this.id!!,
            title = this.title,
            maxGrade = this.maxGrade,
            createdAt = this.createdAt!!,
            deadline = this.deadline,
            authorUsername = this.author.username
        )
    }
}
