package com.example.teamroomback.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "profiles")
data class Profile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "first_name", length = 32, nullable = false)
    var firstName: String,

    @Column(name = "last_name", length = 32)
    var lastName: String? = null,

    @Column(name = "biography", length = 100)
    var biography: String? = null,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,
) {
    /*override fun toString(): String {
        return "Profile(id=$id, firstName='$firstName', lastName='$lastName', biography='$biography', photoUrl=$photoUrl)"
    }*/
}