package com.example.teamroomback

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TeamRoomBackApplication

fun main(args: Array<String>) {
    dotenv {
        directory = "./"
        filename = ".env"
    }.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    runApplication<TeamRoomBackApplication>(*args)
}
