package com.example.teamroomback.entities

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
)