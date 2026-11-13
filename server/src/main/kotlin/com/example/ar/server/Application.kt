package com.example.ar.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.ar.cognokinet.RubricDefaults
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.security.SecureRandom
import java.net.URI
import java.util.*

@Serializable
data class GoogleLoginRequest(
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
data class RefreshTokenRequest(val refreshToken: String)

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
data class ClassStudentResponse(
    val profile: StudentProfile,
    val enrollmentStatus: String,
    val progress: StudentProgress? = null,
    val progressUpdatedAt: Long? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val role: String,
    val classId: String? = null,
    val displayName: String = "",
    val age: Int = 0,
    val grade: String = "",
    val avatarId: String = "",
    val consentGiven: Boolean = false,
    val progress: StudentProgress? = null,
    val refreshToken: String? = null
)

@Serializable
data class CreateClassRequest(
    val name: String,
    val grade: String,
    val group: String,
    val schoolYear: String
)

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val isProduction = System.getenv("APP_ENV") == "production"
    val secret = System.getenv("JWT_SECRET")
        ?: if (isProduction) error("JWT_SECRET must be configured in production")
        else "local-dev-only-change-before-deploy-cognokinet"
    val googleWebClientId = System.getenv("GOOGLE_WEB_CLIENT_ID")
        ?: if (isProduction) error("GOOGLE_WEB_CLIENT_ID must be configured in production")
        else "619352913012-f0avb6ir3m54v56445oajdalardqhued.apps.googleusercontent.com"
    DbFactory.init()

    val issuer = "http://cognokinet.server"
    val audience = "cognokinet_app"

    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        if (!isProduction) {
            anyHost()
        } else {
            System.getenv("CORS_ALLOWED_ORIGIN")?.let { origin ->
                val uri = URI(origin)
                allowHost(uri.host, schemes = listOf(uri.scheme))
            }
        }
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
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

    fun hashRefreshToken(token: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun issueRefreshToken(userId: String, role: String): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val now = System.currentTimeMillis()
        transaction {
            RefreshTokensTable.insert {
                it[tokenHash] = hashRefreshToken(rawToken)
                it[this.userId] = userId
                it[this.role] = role
                it[expiresAt] = now + 30L * 24 * 60 * 60 * 1000
                it[createdAt] = now
            }
        }
        return rawToken
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
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        route("/auth") {
            post("/refresh") {
                val request = call.receive<RefreshTokenRequest>()
                val now = System.currentTimeMillis()
                val oldHash = hashRefreshToken(request.refreshToken)
                val rotated = transaction {
                    val old = RefreshTokensTable.selectAll()
                        .where { RefreshTokensTable.tokenHash eq oldHash }
                        .firstOrNull() ?: return@transaction null
                    if (old[RefreshTokensTable.expiresAt] <= now) {
                        RefreshTokensTable.deleteWhere { tokenHash eq oldHash }
                        return@transaction null
                    }
                    val userId = old[RefreshTokensTable.userId]
                    val role = old[RefreshTokensTable.role]
                    val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
                    val newToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                    RefreshTokensTable.deleteWhere { tokenHash eq oldHash }
                    RefreshTokensTable.insert {
                        it[tokenHash] = hashRefreshToken(newToken)
                        it[this.userId] = userId
                        it[this.role] = role
                        it[expiresAt] = now + 30L * 24 * 60 * 60 * 1000
                        it[createdAt] = now
                    }
                    Triple(userId, role, newToken)
                } ?: return@post call.respond(HttpStatusCode.Unauthorized, "La sesión caducó; inicia sesión de nuevo")

                val (userId, role, newRefreshToken) = rotated
                val classId = if (role == "STUDENT") transaction {
                    EnrollmentsTable.selectAll()
                        .where { EnrollmentsTable.studentId eq userId }
                        .firstOrNull()?.get(EnrollmentsTable.classId)
                } else null
                val student = if (role == "STUDENT") transaction {
                    StudentsTable.selectAll().where { StudentsTable.id eq userId }.firstOrNull()
                } else null
                val teacher = if (role == "TEACHER") transaction {
                    TeachersTable.selectAll().where { TeachersTable.id eq userId }.firstOrNull()
                } else null
                val progress = if (role == "STUDENT") transaction {
                    StudentProgressTable.selectAll().where { StudentProgressTable.studentId eq userId }
                        .firstOrNull()?.let { row -> runCatching {
                            Json { ignoreUnknownKeys = true }
                                .decodeFromString<StudentProgress>(row[StudentProgressTable.progressJson])
                        }.getOrNull() }
                } else null
                call.respond(
                    AuthResponse(
                        token = generateToken(userId, role, classId),
                        userId = userId,
                        role = role,
                        classId = classId,
                        displayName = student?.get(StudentsTable.displayName)
                            ?: teacher?.get(TeachersTable.displayName).orEmpty(),
                        age = student?.get(StudentsTable.age) ?: 0,
                        grade = student?.get(StudentsTable.grade).orEmpty(),
                        avatarId = student?.get(StudentsTable.avatarId).orEmpty(),
                        consentGiven = student?.get(StudentsTable.consentGiven) ?: false,
                        progress = progress,
                        refreshToken = newRefreshToken
                    )
                )
            }

            post("/logout") {
                val request = call.receive<RefreshTokenRequest>()
                transaction {
                    RefreshTokensTable.deleteWhere { tokenHash eq hashRefreshToken(request.refreshToken) }
                }
                call.respond(HttpStatusCode.NoContent)
            }

            post("/google") {
                val request = call.receive<GoogleLoginRequest>()
                val verifiedToken = try {
                    GoogleIdTokenVerifier.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance()
                    ).setAudience(listOf(googleWebClientId)).build().verify(request.idToken)
                } catch (_: Exception) {
                    return@post call.respond(HttpStatusCode.ServiceUnavailable, "No se pudo verificar la cuenta de Google")
                } ?: return@post call.respond(HttpStatusCode.Unauthorized, "El token de Google no es válido")

                val subject = verifiedToken.payload.subject
                val email = verifiedToken.payload.email ?: ""
                val googleName = verifiedToken.payload["name"] as? String ?: ""
                if (subject.isNullOrBlank() || email.isBlank() || verifiedToken.payload.emailVerified != true) {
                    return@post call.respond(HttpStatusCode.Unauthorized, "La cuenta de Google no tiene un correo verificado")
                }

                val role = request.role.uppercase()
                if (role != "TEACHER" && role != "STUDENT") {
                    return@post call.respond(HttpStatusCode.BadRequest, "Rol inválido")
                }

                try {
                    val existing = transaction {
                        GoogleAccountsTable.selectAll()
                            .where { GoogleAccountsTable.googleSubject eq subject }
                            .firstOrNull()
                    }
                    if (existing != null) {
                        if (existing[GoogleAccountsTable.role] != role) {
                            return@post call.respond(HttpStatusCode.Conflict, "Esta cuenta ya está registrada con otro rol")
                        }
                        val userId = existing[GoogleAccountsTable.userId]
                        val classId = if (role == "STUDENT") transaction {
                            EnrollmentsTable.selectAll()
                                .where { EnrollmentsTable.studentId eq userId }
                                .firstOrNull()?.get(EnrollmentsTable.classId)
                        } else null
                        val profile = if (role == "STUDENT") transaction {
                            StudentsTable.selectAll().where { StudentsTable.id eq userId }.firstOrNull()
                        } else null
                        val teacher = if (role == "TEACHER") transaction {
                            TeachersTable.selectAll().where { TeachersTable.id eq userId }.firstOrNull()
                        } else null
                        val savedProgress = if (role == "STUDENT") transaction {
                            StudentProgressTable.selectAll().where { StudentProgressTable.studentId eq userId }.firstOrNull()
                                ?.let { row -> runCatching {
                                    Json { ignoreUnknownKeys = true }
                                        .decodeFromString<StudentProgress>(row[StudentProgressTable.progressJson])
                                }.getOrNull() }
                        } else null
                        return@post call.respond(
                            AuthResponse(
                                token = generateToken(userId, role, classId),
                                userId = userId,
                                role = role,
                                classId = classId,
                                displayName = profile?.get(StudentsTable.displayName)
                                    ?: teacher?.get(TeachersTable.displayName).orEmpty(),
                                age = profile?.get(StudentsTable.age) ?: 0,
                                grade = profile?.get(StudentsTable.grade).orEmpty(),
                                avatarId = profile?.get(StudentsTable.avatarId).orEmpty(),
                                consentGiven = profile?.get(StudentsTable.consentGiven) ?: false,
                                progress = savedProgress,
                                refreshToken = issueRefreshToken(userId, role)
                            )
                        )
                    }

                    val displayName = request.displayName.trim().ifBlank { googleName }.take(128)
                    if (displayName.isBlank()) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Falta el nombre del perfil")
                    }
                    val userId = IdGenerator.newId()
                    val now = System.currentTimeMillis()
                    var classId: String? = null
                    var initialProgress: StudentProgress? = null

                    transaction {
                        if (role == "TEACHER") {
                            val emailAlreadyUsed = TeachersTable.selectAll()
                                .where { TeachersTable.email eq email }
                                .firstOrNull()
                            if (emailAlreadyUsed != null) {
                                throw IllegalStateException("EMAIL_ALREADY_REGISTERED")
                            }
                            TeachersTable.insert {
                                it[id] = userId
                                it[this.displayName] = displayName
                                it[this.email] = email
                                it[passwordHash] = "GOOGLE"
                                it[createdAt] = now
                            }
                        } else {
                            if (request.joinCode.isBlank() || request.age !in 3..18 ||
                                request.grade.isBlank() || !request.consentGiven
                            ) {
                                throw IllegalArgumentException("STUDENT_PROFILE_REQUIRED")
                            }
                            val classroom = ClassroomsTable.selectAll()
                                .where { ClassroomsTable.joinCode eq request.joinCode.trim().uppercase() }
                                .firstOrNull() ?: throw NoSuchElementException("CLASS_NOT_FOUND")
                            if (!classroom[ClassroomsTable.joinOpen]) {
                                throw UnsupportedOperationException("CLASS_CLOSED")
                            }
                            classId = classroom[ClassroomsTable.id]
                            StudentsTable.insert {
                                it[id] = userId
                                it[this.displayName] = displayName
                                it[age] = request.age
                                it[grade] = request.grade.take(64)
                                it[avatarId] = request.avatarId.take(64)
                                it[consentGiven] = true
                                it[createdAt] = now
                            }
                            EnrollmentsTable.insert {
                                it[this.classId] = classId!!
                                it[this.studentId] = userId
                                it[pinHash] = "GOOGLE"
                                it[status] = EnrollmentStatus.PENDING.name
                                it[joinedAt] = now
                            }
                            initialProgress = StudentProgress.inicial(userId)
                            StudentProgressTable.insert {
                                it[studentId] = userId
                                it[progressJson] = Json.encodeToString(initialProgress!!)
                                it[updatedAt] = now
                            }
                        }
                        GoogleAccountsTable.insert {
                            it[googleSubject] = subject
                            it[this.email] = email.take(128)
                            it[this.role] = role
                            it[this.userId] = userId
                            it[createdAt] = now
                        }
                    }

                    call.respond(
                        if (role == "STUDENT") HttpStatusCode.Created else HttpStatusCode.Created,
                        AuthResponse(
                            token = generateToken(userId, role, classId),
                            userId = userId,
                            role = role,
                            classId = classId,
                            displayName = displayName,
                            age = if (role == "STUDENT") request.age else 0,
                            grade = if (role == "STUDENT") request.grade else "",
                            avatarId = if (role == "STUDENT") request.avatarId else "",
                            consentGiven = role == "STUDENT" && request.consentGiven,
                            progress = initialProgress,
                            refreshToken = issueRefreshToken(userId, role)
                        )
                    )
                } catch (e: IllegalStateException) {
                    if (e.message == "EMAIL_ALREADY_REGISTERED") {
                        call.respond(HttpStatusCode.Conflict, "El correo ya tiene una cuenta de profesor")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "No se pudo crear la cuenta")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Perfil inválido")
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, "Código de clase inexistente")
                } catch (e: UnsupportedOperationException) {
                    call.respond(HttpStatusCode.Forbidden, "La clase está cerrada")
                } catch (_: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, "No se pudo crear la cuenta")
                }
            }

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
                    get("/students") {
                        val principal = call.principal<JWTPrincipal>()!!
                        val role = principal.payload.getClaim("role").asString()
                        if (role != "TEACHER") {
                            return@get call.respond(HttpStatusCode.Forbidden, "Solo un profesor puede ver la lista")
                        }
                        val classId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                        val teacherId = principal.payload.getClaim("userId").asString()
                        val ownsClass = transaction {
                            ClassroomsTable.selectAll()
                                .where { (ClassroomsTable.id eq classId) and (ClassroomsTable.teacherId eq teacherId) }
                                .firstOrNull() != null
                        }
                        if (!ownsClass) return@get call.respond(HttpStatusCode.NotFound, "Clase no encontrada")

                        val records = transaction {
                            EnrollmentsTable.selectAll().where { EnrollmentsTable.classId eq classId }.mapNotNull { enrollment ->
                                val studentId = enrollment[EnrollmentsTable.studentId]
                                val student = StudentsTable.selectAll()
                                    .where { StudentsTable.id eq studentId }.firstOrNull() ?: return@mapNotNull null
                                val progressRow = StudentProgressTable.selectAll()
                                    .where { StudentProgressTable.studentId eq studentId }.firstOrNull()
                                val progress = progressRow?.let {
                                    runCatching {
                                        Json { ignoreUnknownKeys = true }
                                            .decodeFromString<StudentProgress>(it[StudentProgressTable.progressJson])
                                    }.getOrNull()
                                }
                                ClassStudentResponse(
                                    profile = StudentProfile(
                                        id = student[StudentsTable.id],
                                        displayName = student[StudentsTable.displayName],
                                        age = student[StudentsTable.age],
                                        grade = student[StudentsTable.grade],
                                        avatarId = student[StudentsTable.avatarId],
                                        consentGiven = student[StudentsTable.consentGiven],
                                        createdAt = student[StudentsTable.createdAt]
                                    ),
                                    enrollmentStatus = enrollment[EnrollmentsTable.status],
                                    progress = progress,
                                    progressUpdatedAt = progressRow?.get(StudentProgressTable.updatedAt)
                                )
                            }
                        }
                        call.respond(records)
                    }

                    post("/students/{sid}/approve") {
                        try {
                            val classId = call.parameters["id"]!!
                            val sid = call.parameters["sid"]!!
                            val principal = call.principal<JWTPrincipal>()!!
                            val teacherId = principal.payload.getClaim("userId").asString()
                            val ownsClass = transaction {
                                ClassroomsTable.selectAll()
                                    .where { (ClassroomsTable.id eq classId) and (ClassroomsTable.teacherId eq teacherId) }
                                    .firstOrNull() != null
                            }
                            if (principal.payload.getClaim("role").asString() != "TEACHER" || !ownsClass) {
                                return@post call.respond(HttpStatusCode.NotFound, "Clase no encontrada")
                            }

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
                            val principal = call.principal<JWTPrincipal>()!!
                            val teacherId = principal.payload.getClaim("userId").asString()
                            val ownsClass = transaction {
                                ClassroomsTable.selectAll()
                                    .where { (ClassroomsTable.id eq classId) and (ClassroomsTable.teacherId eq teacherId) }
                                    .firstOrNull() != null
                            }
                            if (principal.payload.getClaim("role").asString() != "TEACHER" || !ownsClass) {
                                return@post call.respond(HttpStatusCode.NotFound, "Clase no encontrada")
                            }

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

                get("/progress") {
                    val principal = call.principal<JWTPrincipal>()!!
                    if (principal.payload.getClaim("role").asString() != "STUDENT") {
                        return@get call.respond(HttpStatusCode.Forbidden, "Solo un alumno puede consultar su progreso")
                    }
                    val studentId = principal.payload.getClaim("userId").asString()
                    val progress = transaction {
                        StudentProgressTable.selectAll()
                            .where { StudentProgressTable.studentId eq studentId }
                            .firstOrNull()
                            ?.let { row -> runCatching {
                                Json { ignoreUnknownKeys = true }
                                    .decodeFromString<StudentProgress>(row[StudentProgressTable.progressJson])
                            }.getOrNull() }
                    } ?: StudentProgress.inicial(studentId)
                    call.respond(progress)
                }

                put("/progress") {
                    val principal = call.principal<JWTPrincipal>()!!
                    if (principal.payload.getClaim("role").asString() != "STUDENT") {
                        return@put call.respond(HttpStatusCode.Forbidden, "Solo un alumno puede sincronizar su progreso")
                    }
                    val studentId = principal.payload.getClaim("userId").asString()
                    val progress = call.receive<StudentProgress>()
                    if (progress.profileId != studentId) {
                        return@put call.respond(HttpStatusCode.Forbidden, "El progreso no corresponde a esta cuenta")
                    }
                    val json = Json.encodeToString(progress)
                    transaction {
                        val existing = StudentProgressTable.selectAll()
                            .where { StudentProgressTable.studentId eq studentId }.firstOrNull()
                        if (existing == null) {
                            StudentProgressTable.insert {
                                it[this.studentId] = studentId
                                it[progressJson] = json
                                it[updatedAt] = System.currentTimeMillis()
                            }
                        } else {
                            StudentProgressTable.update({ StudentProgressTable.studentId eq studentId }) {
                                it[progressJson] = json
                                it[updatedAt] = System.currentTimeMillis()
                            }
                        }
                    }
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
