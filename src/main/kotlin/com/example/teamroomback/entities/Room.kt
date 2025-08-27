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
@Table(name = "rooms")
data class Room (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,

    @Column(name = "name", length = 512, nullable = false)
    val name: String,

    @Column(name = "photo_url")
    val photoUrl: String? = null,

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val roomMembers: List<RoomMember> = listOf(),

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val roomMessages: List<RoomMessage> = listOf(),
)