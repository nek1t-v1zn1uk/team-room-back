package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository : JpaRepository<Profile, Long> {
    fun findByUserUsernameValue(username: String): Profile?
}