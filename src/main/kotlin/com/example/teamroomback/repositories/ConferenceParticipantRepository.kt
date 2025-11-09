package com.example.teamroomback.repositories

import com.example.teamroomback.entities.ConferenceParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConferenceParticipantRepository: JpaRepository<ConferenceParticipant, Long> {
    fun findByConferenceIdAndUserUsernameValue(conferenceId: Long, username: String): ConferenceParticipant?
}