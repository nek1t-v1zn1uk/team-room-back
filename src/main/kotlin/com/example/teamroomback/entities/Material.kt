package com.example.teamroomback.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "materials")
data class Material(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "topic", nullable = false)
    var topic: String,

    @Column(name = "text_content")
    var textContent: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val media: List<MaterialMedia> = listOf(),

    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val tags: List<MaterialTag> = listOf(),

)