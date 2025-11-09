package com.example.teamroomback.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "courses")
data class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", length = 100, nullable = false)
    var name: String,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @Column(name = "is_open")
    var isOpen: Boolean = true,

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val courseMembers: List<CourseMember> = listOf(),

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val materials: List<Material> = listOf(),

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val assignments: List<Assignment> = listOf(),

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val chats: List<Chat> = listOf(),

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val conferences: List<Conference> = listOf(),
)