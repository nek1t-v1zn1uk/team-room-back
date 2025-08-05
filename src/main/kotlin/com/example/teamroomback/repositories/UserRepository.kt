package com.example.teamroomback.repositories

import com.example.teamroomback.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsernameValue(username: String): User?
    fun findByEmail(email: String): User?
}