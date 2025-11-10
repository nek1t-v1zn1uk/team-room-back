package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.JitsiEventDTO
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.services.JitsiService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jitsi")
@Tag(name = "Jitsi Webhooks", description = "Ендпоїнти для Jitsi Webhooks.")
class JitsiController(
    private val jitsiService: JitsiService,
) {

    @PostMapping("/event")
    @Operation(summary = "Обробити подію Jitsi.")
    fun handleEvent(@RequestBody request: JitsiEventDTO): ResponseEntity<SimpleMessageResponse> {
        jitsiService.handleEvent(request)

        return ResponseEntity.ok(SimpleMessageResponse("Event received"))
    }

}