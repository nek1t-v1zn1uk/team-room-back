package com.example.teamroomback.entities

import com.example.teamroomback.dtos.AssignmentResponseDTO
import com.example.teamroomback.dtos.AssignmentResponseMediaDTO
import com.example.teamroomback.dtos.AssignmentResponseShortDTO
import jakarta.persistence.*

@Entity
@Table(name = "assignment_responses")
data class AssignmentResponse(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    var assignment: Assignment,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @Column(name = "is_returned")
    var isReturned: Boolean = false,

    @Column(name = "return_comment", columnDefinition = "text")
    var returnComment: String? = null,

    @Column(name = "is_graded")
    var isGraded: Boolean = false,

    @Column(name = "grade")
    var grade: Int? = null,

    @Column(name = "grade_comment", columnDefinition = "text")
    var gradeComment: String? = null,

    @OneToMany(mappedBy = "assignmentResponse", cascade = [CascadeType.ALL], orphanRemoval = true)
    val media: List<AssignmentResponseMedia> = listOf()
) {

    fun toAssignmentResponseDTO(): AssignmentResponseDTO {
        return AssignmentResponseDTO(
            id = this.id!!,
            authorUsername = this.author.username,
            isGraded = this.isGraded,
            grade = this.grade,
            gradeComment = this.gradeComment,
            isReturned = this.isReturned,
            returnComment = this.returnComment,
            media = this.media.map { it.toAssignmentResponseMediaDTO() }
        )
    }

    fun toAssignmentResponseShortDTO(): AssignmentResponseShortDTO {
        return AssignmentResponseShortDTO(
            id = this.id!!,
            authorUsername = this.author.username,
            isGraded = this.isGraded,
            grade = this.grade,
            isReturned = this.isReturned
        )
    }
}