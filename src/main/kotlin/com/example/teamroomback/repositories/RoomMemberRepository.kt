package com.example.teamroomback.repositories

import com.example.teamroomback.entities.RoomMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomMemberRepository: JpaRepository<RoomMember, Long> {
}