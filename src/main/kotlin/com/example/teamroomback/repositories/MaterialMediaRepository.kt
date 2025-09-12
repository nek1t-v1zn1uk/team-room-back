package com.example.teamroomback.repositories

import com.example.teamroomback.entities.MaterialMedia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MaterialMediaRepository : JpaRepository<MaterialMedia, Long> {

}