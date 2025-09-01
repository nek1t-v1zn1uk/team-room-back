package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.DownloadLinkResponse
import com.example.teamroomback.dtos.UploadLinkResponse
import com.example.teamroomback.services.PCloudService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cloud-storage")
class PCloudController(
    private val service: PCloudService
){

    @GetMapping("/get-upload-link")
    fun getUploadLink(@RequestParam purpose: String): ResponseEntity<Any> {
        return ResponseEntity.ok(UploadLinkResponse(
            link = service.getUploadLink(purpose)
        ))
    }

    @GetMapping("/get-download-link")
    fun getUploadLink(@RequestParam fileId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(DownloadLinkResponse(
            link = service.getDownloadLink(fileId)
        ))
    }

}