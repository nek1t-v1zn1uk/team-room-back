package com.example.teamroomback.repositories

import com.example.teamroomback.entities.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsernameValue(username: String): User?
    fun findByEmail(email: String): User?
    fun findByUsernameValueContainingIgnoreCase(username: String): List<User>

    @Modifying
    @Transactional
    fun deleteByUsernameValue(username: String): Int
}