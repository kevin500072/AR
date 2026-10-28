package com.example.ar.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.ar.cognokinet.RubricDefaults
import com.example.ar.cognokinet.domain.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

@Serializable
data class TeacherRegisterRequest(
    val displayName: String,
    val email: String,
    val password: String
)

@Serializable
data class TeacherLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class StudentJoinRequest(
    val joinCode: String,
    val displayName: String,
    val age: Int,
    val grade: String,
    val avatarId: String,
    val consentGiven: Boolean = false,
    val pin: String
)

@Serializable
data class StudentLoginRequest(
    val joinCode: String,
    val studentId: String,
    val pin: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val role: String,
    val classId: String? = null
)

@Serializable
data class CreateClassRequest(
    val name: String,
    val grade: String,
    val group: String,
    val schoolYear: String
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DbFactory.init(inMemory = true)

    val secret = "cognokinet_jwt_secret_key_12345"
    val issuer = "http://cognokinet.server"
    val audience = "cognokinet_app"

    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "cognokinet"
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    fun generateToken(userId: String, role: String, classId: String? = null): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withClaim("classId", classId ?: "")
            .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000))
            .sign(Algorithm.HMAC256(secret))
    }

    routing {
        route("/auth") {
            post("/teacher/register") {
                try {
                    val req = call.receive<TeacherRegisterRequest>()
                    if (req.email.isBlank() || req.password.length < 6) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Email o contraseña inválidos")
                    }

                    val existing = transaction {
                        TeachersTable.selectAll().where { TeachersTable.email eq req.email }.firstOrNull()
                    }
                    if (existing != null) {
                        return@post call.respond(HttpStatusCode.Conflict, "El correo ya está registrado")
                    }

                    val teacherId = IdGenerator.newId()
                    val passHash = BCrypt.hashpw(req.password, BCrypt.gensalt())

                    transaction {
                        TeachersTable.insert {
                            it[id] = teacherId
                            it[displayName] = req.displayName
                            it[email] = req.email
                            it[passwordHash] = passHash
                            it[createdAt] = System.currentTimeMillis()
                        }
                    }

                    val token = generateToken(teacherId, "TEACHER")
                    call.respond(AuthResponse(token = token, userId = teacherId, role = "TEACHER"))
                } catch (e: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error interno")
                }
            }

            post("/teacher/login") {
                try {
                    val req = call.receive<TeacherLoginRequest>()
                    val teacherRow = transaction {
                        TeachersTable.selectAll().where { TeachersTable.email eq req.email }.firstOrNull()
                    } ?: return@post call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")

                    val storedHash = teacherRow[TeachersTable.passwordHash]
                    if (!BCrypt.checkpw(req.password, storedHash)) {
                        return@post call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                    }

                    val teacherId = teacherRow[TeachersTable.id]
                    val token = generateToken(teacherId, "TEACHER")
                    call.respond(AuthResponse(token = token, userId = teacherId, role = "TEACHER"))
                } catch (e: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                }
            }

            post("/student/join") {
                try {
                    val req = call.receive<StudentJoinRequest>()
                    val code = req.joinCode.uppercase().trim()

                    val classRow = transaction {
                        ClassroomsTable.selectAll().where { ClassroomsTable.joinCode eq code }.firstOrNull()
                    } ?: return@post call.respond(HttpStatusCode.NotFound, "Código de clase inexistente")

                    if (!classRow[ClassroomsTable.joinOpen]) {
                        return@post call.respond(HttpStatusCode.Forbidden, "La clase está cerrada para nuevos ingresos")
                    }

                    val classId = classRow[ClassroomsTable.id]
                    val studentId = IdGenerator.newId()
                    val pinHash = BCrypt.hashpw(req.pin, BCrypt.gensalt())

                    transaction {
                        StudentsTable.insert {
                            it[id] = studentId
                            it[displayName] = req.displayName
                            it[age] = req.age
                            it[grade] = req.grade
                            it[avatarId] = req.avatarId
                            it[consentGiven] = req.consentGiven
                            it[createdAt] = System.currentTimeMillis()
                        }

                        EnrollmentsTable.insert {
                            it[this.classId] = classId
                            it[this.studentId] = studentId
                            it[this.pinHash] = pinHash
                            it[status] = EnrollmentStatus.PENDING.name
                            it[joinedAt] = System.currentTimeMillis()
                        }
                    }

                    call.respond(HttpStatusCode.Created, mapOf("studentId" to studentId, "status" to "PENDING", "message" to "Esperando aprobación de la profesora"))
                } catch (e: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                }
            }

            post("/student/login") {
                try {
                    val req = call.receive<StudentLoginRequest>()
                    val code = req.joinCode.uppercase().trim()

                    val classRow = transaction {
                        ClassroomsTable.selectAll().where { ClassroomsTable.joinCode eq code }.firstOrNull()
                    } ?: return@post call.respond(HttpStatusCode.NotFound, "Código de clase inexistente")

                    val classId = classRow[ClassroomsTable.id]

                    val enrollment = transaction {
                        EnrollmentsTable.selectAll().where { (EnrollmentsTable.classId eq classId) and (EnrollmentsTable.studentId eq req.studentId) }.firstOrNull()
                    } ?: return@post call.respond(HttpStatusCode.Unauthorized, "Alumno no encontrado en esta clase")

                    if (enrollment[EnrollmentsTable.status] != EnrollmentStatus.ACTIVE.name) {
                        return@post call.respond(HttpStatusCode.Forbidden, "Tu ingreso aún no ha sido aprobado por la profesora")
                    }

                    if (!BCrypt.checkpw(req.pin, enrollment[EnrollmentsTable.pinHash])) {
                        return@post call.respond(HttpStatusCode.Unauthorized, "PIN incorrecto")
                    }

                    val token = generateToken(req.studentId, "STUDENT", classId)
                    call.respond(AuthResponse(token = token, userId = req.studentId, role = "STUDENT", classId = classId))
                } catch (e: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                }
            }
        }

        authenticate("auth-jwt") {
            route("/classes") {
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()!!
                        val teacherId = principal.payload.getClaim("userId").asString()
                        val req = call.receive<CreateClassRequest>()

                        val teacherRow = transaction { TeachersTable.selectAll().where { TeachersTable.id eq teacherId }.firstOrNull() }
                        val teacherName = teacherRow?.get(TeachersTable.displayName) ?: "Prof. Guía"

                        val classId = IdGenerator.newId()
                        var joinCode = ClassCodeGenerator.generateCode()

                        transaction {
                            while (ClassroomsTable.selectAll().where { ClassroomsTable.joinCode eq joinCode }.count() > 0) {
                                joinCode = ClassCodeGenerator.generateCode()
                            }

                            ClassroomsTable.insert {
                                it[id] = classId
                                it[name] = req.name
                                it[grade] = req.grade
                                it[group] = req.group
                                it[schoolYear] = req.schoolYear
                                it[this.teacherId] = teacherId
                                it[this.joinCode] = joinCode
                                it[joinOpen] = true
                                it[createdAt] = System.currentTimeMillis()
                            }

                            val defaultRubrics = RubricDefaults.forAllStations(classId)
                            defaultRubrics.forEach { rubric ->
                                RubricsTable.insert {
                                    it[id] = rubric.id
                                    it[this.classId] = classId
                                    it[stationId] = rubric.stationId
                                }
                                rubric.criteria.forEach { criterion ->
                                    RubricCriteriaTable.insert {
                                        it[id] = criterion.id
                                        it[rubricId] = rubric.id
                                        it[name] = criterion.name
                                        it[weight] = criterion.weight
                                    }
                                    criterion.levels.forEach { level ->
                                        RubricLevelsTable.insert {
                                            it[id] = IdGenerator.newId()
                                            it[criterionId] = criterion.id
                                            it[points] = level.points
                                            it[label] = level.label
                                            it[descriptor] = level.descriptor
                                        }
                                    }
                                }
                            }
                        }

                        val classroom = Classroom(
                            id = classId,
                            name = req.name,
                            grade = req.grade,
                            group = req.group,
                            schoolYear = req.schoolYear,
                            teacherId = teacherId,
                            teacherName = teacherName,
                            joinCode = joinCode,
                            joinOpen = true,
                            createdAt = System.currentTimeMillis()
                        )
                        call.respond(HttpStatusCode.Created, classroom)
                    } catch (e: Throwable) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                    }
                }

                get {
                    try {
                        val principal = call.principal<JWTPrincipal>()!!
                        val teacherId = principal.payload.getClaim("userId").asString()

                        val list = transaction {
                            ClassroomsTable.selectAll().where { ClassroomsTable.teacherId eq teacherId }.map {
                                Classroom(
                                    id = it[ClassroomsTable.id],
                                    name = it[ClassroomsTable.name],
                                    grade = it[ClassroomsTable.grade],
                                    group = it[ClassroomsTable.group],
                                    schoolYear = it[ClassroomsTable.schoolYear],
                                    teacherId = it[ClassroomsTable.teacherId],
                                    teacherName = "",
                                    joinCode = it[ClassroomsTable.joinCode],
                                    joinOpen = it[ClassroomsTable.joinOpen],
                                    createdAt = it[ClassroomsTable.createdAt]
                                )
                            }
                        }
                        call.respond(list)
                    } catch (e: Throwable) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                    }
                }

                route("/{id}") {
                    post("/students/{sid}/approve") {
                        try {
                            val classId = call.parameters["id"]!!
                            val sid = call.parameters["sid"]!!

                            transaction {
                                EnrollmentsTable.update({ (EnrollmentsTable.classId eq classId) and (EnrollmentsTable.studentId eq sid) }) {
                                    it[status] = EnrollmentStatus.ACTIVE.name
                                }
                            }
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Alumno aprobado"))
                        } catch (e: Throwable) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                        }
                    }

                    post("/students/{sid}/reject") {
                        try {
                            val classId = call.parameters["id"]!!
                            val sid = call.parameters["sid"]!!

                            transaction {
                                EnrollmentsTable.update({ (EnrollmentsTable.classId eq classId) and (EnrollmentsTable.studentId eq sid) }) {
                                    it[status] = EnrollmentStatus.REJECTED.name
                                }
                            }
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Alumno rechazado"))
                        } catch (e: Throwable) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                        }
                    }
                }
            }

            route("/me") {
                get("/classes") {
                    try {
                        val principal = call.principal<JWTPrincipal>()!!
                        val studentId = principal.payload.getClaim("userId").asString()

                        val enrollment = transaction {
                            EnrollmentsTable.selectAll().where { EnrollmentsTable.studentId eq studentId }.firstOrNull()
                        } ?: return@get call.respond(HttpStatusCode.NotFound, "Sin clase asignada")

                        val classId = enrollment[EnrollmentsTable.classId]
                        val classRow = transaction { ClassroomsTable.selectAll().where { ClassroomsTable.id eq classId }.firstOrNull() }
                            ?: return@get call.respond(HttpStatusCode.NotFound, "Clase no encontrada")

                        val teacherRow = transaction { TeachersTable.selectAll().where { TeachersTable.id eq classRow[ClassroomsTable.teacherId] }.firstOrNull() }
                        val teacherName = teacherRow?.get(TeachersTable.displayName) ?: "Profesora"

                        val classroom = Classroom(
                            id = classRow[ClassroomsTable.id],
                            name = classRow[ClassroomsTable.name],
                            grade = classRow[ClassroomsTable.grade],
                            group = classRow[ClassroomsTable.group],
                            schoolYear = classRow[ClassroomsTable.schoolYear],
                            teacherId = classRow[ClassroomsTable.teacherId],
                            teacherName = teacherName,
                            joinCode = classRow[ClassroomsTable.joinCode],
                            joinOpen = classRow[ClassroomsTable.joinOpen],
                            createdAt = classRow[ClassroomsTable.createdAt]
                        )

                        call.respond(classroom)
                    } catch (e: Throwable) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                    }
                }
            }
        }
    }
}
