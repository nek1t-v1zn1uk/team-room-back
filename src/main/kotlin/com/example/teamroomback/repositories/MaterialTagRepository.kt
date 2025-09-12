package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Material
import com.example.teamroomback.entities.MaterialTag
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

@Repository
interface MaterialTagRepository : JpaRepository<MaterialTag, Long> {
    @Modifying
    @Transactional
    fun deleteMaterialTagsByMaterialId(id: Long): Int
}