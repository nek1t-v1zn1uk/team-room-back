package com.example.teamroomback.services

import com.example.teamroomback.dtos.PatchProfileRequest
import com.example.teamroomback.dtos.PutProfileRequest
import com.example.teamroomback.entities.Profile
import com.example.teamroomback.repositories.ProfileRepository
import com.example.teamroomback.repositories.UserRepository
import org.springframework.stereotype.Service
import javax.management.InstanceNotFoundException

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
) {
    fun createProfile(
        username: String,
        firstName: String,
        lastName: String? = null,
        biography: String? = null,
        photoUrl: String? = null,
    ): Profile {

        val profile = Profile(
            firstName = firstName,
            lastName = lastName,
            biography = biography,
            photoUrl = photoUrl,
            user = userRepository.findByUsernameValue(username)!!
        )
        return profileRepository.save(profile)
    }

    fun getProfile(username: String): Profile {
        return profileRepository.findByUserUsernameValue(username)
            ?: throw NoSuchElementException("Profile for user \"$username\" not found")
    }

    fun putProfile(username: String, request: PutProfileRequest): Profile {
        val profile = getProfile(username)
        profile.firstName = request.firstName
        profile.lastName = request.lastName
        profile.biography = request.biography
        profile.photoUrl = request.photoUrl

        return profileRepository.save(profile)
    }

    fun patchProfile(username: String, request: PatchProfileRequest): Profile {
        val profile = getProfile(username)
        request.firstName?.let { profile.firstName = it }
        request.lastName?.let { profile.lastName = it }
        request.biography?.let { profile.biography = it }
        request.photoUrl?.let { profile.photoUrl = it }

        return profileRepository.save(profile)
    }
}