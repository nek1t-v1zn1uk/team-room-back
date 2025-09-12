package com.example.teamroomback.repositories

import com.example.teamroomback.entities.Material
import com.example.teamroomback.entities.MaterialTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MaterialTagRepository : JpaRepository<MaterialTag, Long> {
}