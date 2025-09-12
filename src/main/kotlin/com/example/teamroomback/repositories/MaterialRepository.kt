package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Material
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

@Repository
interface MaterialRepository : JpaRepository<Material, Long> {
    fun findMaterialById(id: Long): Material?
    fun findMaterialsByCourseId(id: Long): List<Material>

    @Modifying
    @Transactional
    fun deleteMaterialById(id: Long): Int
}