package com.example.teamroomback.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Collections

@RestController
@RequestMapping("/api")
class DefaultController {

    @GetMapping
    fun sayHello(): Map<String, String> {
        return Collections.singletonMap("message", "Hello World!")
    }

}