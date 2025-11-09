package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.JitsiEventDTO
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.services.JitsiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jitsi")
class JitsiController(
    private val jitsiService: JitsiService,
) {

    @PostMapping("/event")
    fun handleEvent(@RequestBody request: JitsiEventDTO): ResponseEntity<SimpleMessageResponse> {
        jitsiService.handleEvent(request)

        return ResponseEntity.ok(SimpleMessageResponse("Event received"))
    }

}