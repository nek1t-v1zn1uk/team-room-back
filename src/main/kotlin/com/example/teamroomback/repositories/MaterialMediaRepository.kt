package com.example.teamroomback.repositories

import com.example.teamroomback.entities.MaterialMedia
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository

@Repository
interface MaterialMediaRepository : JpaRepository<MaterialMedia, Long> {
    @Modifying
    @Transactional
    fun deleteMaterialMediasByMaterialId(id: Long): Int
}