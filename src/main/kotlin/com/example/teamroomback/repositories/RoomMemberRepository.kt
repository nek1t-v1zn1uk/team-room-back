package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Room
import com.example.teamroomback.entities.RoomMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoomMemberRepository: JpaRepository<RoomMember, Long> {
    @Query("select member from RoomMember member " +
            "join fetch member.user user " +
            "join fetch user.profile " +
            "where member.room.id = :roomId")
    fun findRoomMembersByRoomId(roomId: Long): List<RoomMember>
}