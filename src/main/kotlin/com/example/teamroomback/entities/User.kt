package com.example.teamroomback.entities

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", length = 32, unique = true, nullable = false)
    val usernameValue: String,

    @Column(name = "email", length = 254, unique = true, nullable = false)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val profile: Profile? = null,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    val courseMembers: List<CourseMember> = listOf(),

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    val materialsPosted: List<Material> = listOf(),

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], orphanRemoval = true)
    val createdAssignments: List<Assignment> = listOf(),

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], orphanRemoval = true)
    val assignmentResponses: List<AssignmentResponse> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val chatMemberships: List<ChatMember> = listOf(),

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    val chatMessages: List<ChatMessage> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val messageReactions: List<MessageReaction> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participants: MutableList<ConferenceParticipant> = mutableListOf()

) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }
    override fun getUsername(): String = usernameValue
    override fun getPassword(): String = passwordHash
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    /*override fun toString(): String {
        return "User(id=$id, usernameValue='$usernameValue', email='$email', passwordHash='$passwordHash')"
    }*/
}