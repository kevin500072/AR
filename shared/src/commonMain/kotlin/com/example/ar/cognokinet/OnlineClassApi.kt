package com.example.ar.cognokinet

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val API_BASE_URL = "http://127.0.0.1:8080"

@Serializable
data class OnlineGoogleLoginRequest(
    val idToken: String,
    val role: String,
    val joinCode: String = "",
    val displayName: String = "",
    val age: Int = 8,
    val grade: String = "",
    val avatarId: String = "fox",
    val consentGiven: Boolean = false
)

@Serializable
data class OnlineRefreshRequest(val refreshToken: String)

@Serializable
data class OnlineAuthResponse(
    val token: String,
    val userId: String,
    val role: String,
    val classId: String? = null,
    val displayName: String = "",
    val age: Int = 0,
    val grade: String = "",
    val avatarId: String = "",
    val consentGiven: Boolean = false,
    val progress: com.example.ar.cognokinet.domain.StudentProgress? = null,
    val refreshToken: String? = null
)

@Serializable
data class OnlineClassStudent(
    val profile: com.example.ar.cognokinet.domain.StudentProfile,
    val enrollmentStatus: String,
    val progress: com.example.ar.cognokinet.domain.StudentProgress? = null,
    val progressUpdatedAt: Long? = null
)

@Serializable
data class OnlineCreateClassRequest(
    val name: String,
    val grade: String,
    val group: String,
    val schoolYear: String
)

object OnlineClassApi {
    fun encodeSession(session: OnlineAuthResponse): String = Json.encodeToString(session)

    fun decodeSession(value: String): OnlineAuthResponse? =
        runCatching { Json.decodeFromString<OnlineAuthResponse>(value) }.getOrNull()

    private val client = HttpClient {
        expectSuccess = true
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 20_000
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun refreshSession(refreshToken: String): OnlineAuthResponse =
        client.post("$API_BASE_URL/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(OnlineRefreshRequest(refreshToken))
        }.body()

    suspend fun logout(refreshToken: String) {
        client.post("$API_BASE_URL/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(OnlineRefreshRequest(refreshToken))
        }
    }

    suspend fun signInWithGoogle(request: OnlineGoogleLoginRequest): OnlineAuthResponse =
        client.post("$API_BASE_URL/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getClasses(token: String): List<com.example.ar.cognokinet.domain.Classroom> =
        client.get("$API_BASE_URL/classes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    suspend fun getClassStudents(token: String, classId: String): List<OnlineClassStudent> =
        client.get("$API_BASE_URL/classes/$classId/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    suspend fun approveStudent(token: String, classId: String, studentId: String) {
        client.post("$API_BASE_URL/classes/$classId/students/$studentId/approve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun rejectStudent(token: String, classId: String, studentId: String) {
        client.post("$API_BASE_URL/classes/$classId/students/$studentId/reject") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun getProgress(token: String): com.example.ar.cognokinet.domain.StudentProgress =
        client.get("$API_BASE_URL/me/progress") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    suspend fun saveProgress(token: String, progress: com.example.ar.cognokinet.domain.StudentProgress) {
        client.put("$API_BASE_URL/me/progress") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(progress)
        }
    }

    suspend fun createClass(token: String, request: OnlineCreateClassRequest): com.example.ar.cognokinet.domain.Classroom =
        client.post("$API_BASE_URL/classes") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(request)
        }.body()
}
