package com.example.teamroomback.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "course_members")
data class CourseMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "course_member_role")
    val role: CourseMemberRole = CourseMemberRole.STUDENT,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course
)

enum class CourseMemberRole {
    OWNER,
    PROFESSOR,
    LEADER,
    STUDENT,
    VIEWER;

    fun isAtLeast(other: CourseMemberRole): Boolean {
        return this.ordinal <= other.ordinal
    }

    fun canManage(other: CourseMemberRole): Boolean {
        return when (this) {
            OWNER -> true
            PROFESSOR -> other != OWNER && other != PROFESSOR
            LEADER -> other == STUDENT
            else -> false
        }
    }
}