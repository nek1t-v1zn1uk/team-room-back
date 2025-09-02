package com.example.teamroomback.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class PCloudService {

    private val client = OkHttpClient()
    private val hostname = "https://eapi.pcloud.com"
    @Value("\${PCLOUD_TOKEN}")
    private lateinit var token: String
    @Value("\${PCLOUD_USERNAME}")
    private lateinit var authUsername: String
    @Value("\${PCLOUD_PASS}")
    private lateinit var authPass: String
    private val defaultPath = "/Application/team-room"
    private val paths = mutableMapOf(
        "profile-photo" to "/profile_photos"
    )

    fun getUploadLink(purpose: String): String{

        val url = "$hostname/uploadfile?access_token=$token&path=${paths[purpose]}"
        println(token)
        return url
    }

    fun getPubLink(fileId: Long): String {
        val requestPubLink = Request.Builder()
            .url("$hostname/getfilepublink?access_token=$token&fileid=$fileId")
            .build()

        val client = OkHttpClient()

        val pubLinkResponse = client.newCall(requestPubLink).execute()

        if (!pubLinkResponse.isSuccessful) {
            throw IOException("Unexpected code $pubLinkResponse")
        }

        val mapper = ObjectMapper()
        val typeRef = object : TypeReference<Map<String, Any>>() {}
        val pubLinkMap: Map<String, Any> = mapper.readValue(pubLinkResponse.body!!.string(), typeRef)
        val link = pubLinkMap["link"] as String

        /*val requestDownloadLink = Request.Builder()
            .url("$hostname/getpublinkdownload?code=$code")
            .build()

        val downloadLinkResponse = client.newCall(requestDownloadLink).execute()

        if (!downloadLinkResponse.isSuccessful) {
            throw IOException("Unexpected code $downloadLinkResponse")
        }

        val downloadLinkMap: Map<String, Any> = mapper.readValue(downloadLinkResponse.body!!.string(), typeRef)
        val hosts = downloadLinkMap["hosts"] as List<*>
        val host = hosts[0] as String
        val path = downloadLinkMap["path"] as String

        return "https://${host}$path"
        */

        return link
    }


}