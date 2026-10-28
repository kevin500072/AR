package com.example.ar.server

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerIntegrationTest {

    @Test
    fun testFullServerFlow() = testApplication {
        application {
            module()
        }

        val testClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // 1. Teacher Registration
        val regResponse = testClient.post("/auth/teacher/register") {
            contentType(ContentType.Application.Json)
            setBody(TeacherRegisterRequest("Prof. Ana", "ana@escuela.edu", "password123"))
        }
        assertEquals(HttpStatusCode.OK, regResponse.status, "Error body: ${regResponse.bodyAsText()}")
        val regJson = Json.parseToJsonElement(regResponse.bodyAsText()).jsonObject
        val token = regJson["token"]?.jsonPrimitive?.content ?: ""
        assertTrue(token.isNotEmpty())

        // 2. Class Creation
        val createClassResponse = testClient.post("/classes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreateClassRequest("Educación Física 4º A", "4º", "A", "2024-2025"))
        }
        assertEquals(HttpStatusCode.Created, createClassResponse.status)
        val classJson = Json.parseToJsonElement(createClassResponse.bodyAsText()).jsonObject
        val classId = classJson["id"]?.jsonPrimitive?.content ?: ""
        val joinCode = classJson["joinCode"]?.jsonPrimitive?.content ?: ""
        assertEquals(6, joinCode.length)

        // 3. Student Join with valid 6-char code
        val studentJoinResponse = testClient.post("/auth/student/join") {
            contentType(ContentType.Application.Json)
            setBody(StudentJoinRequest(joinCode, "Mateo", 9, "4º A", "fox", true, "1234"))
        }
        assertEquals(HttpStatusCode.Created, studentJoinResponse.status)
        val studentJson = Json.parseToJsonElement(studentJoinResponse.bodyAsText()).jsonObject
        val studentId = studentJson["studentId"]?.jsonPrimitive?.content ?: ""
        assertTrue(studentId.isNotEmpty())

        // 4. Student Join with invalid code fails
        val invalidJoinResponse = testClient.post("/auth/student/join") {
            contentType(ContentType.Application.Json)
            setBody(StudentJoinRequest("BADCODE", "Mateo", 9, "4º A", "fox", true, "1234"))
        }
        assertEquals(HttpStatusCode.NotFound, invalidJoinResponse.status)

        // 5. Approve Student
        val approveResponse = testClient.post("/classes/$classId/students/$studentId/approve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, approveResponse.status)
    }
}
