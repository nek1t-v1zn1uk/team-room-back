package com.example.teamroomback.services

import com.example.teamroomback.entities.User
import com.example.teamroomback.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByUsernameValue(username)
            ?: throw UsernameNotFoundException("User with username \"${username}\" not found")
    }

    fun registerUser(username: String, email: String, passwordPlain: String): User {
        if(userRepository.findByUsernameValue(username) != null) {
            throw IllegalArgumentException("User with username \"$username\" is already registered")
        }
        if(userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("User with email \"$email\" is already registered")
        }
        val passwordHash = passwordEncoder.encode(passwordPlain)
        val newUser = User(
            usernameValue = username,
            email = email,
            passwordHash = passwordHash
        )
        return userRepository.save(newUser)
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByUsernameValue(username)
            ?: throw UsernameNotFoundException("User with username \"${username}\" not found")
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User with email \"${email}\" not found")
    }

}