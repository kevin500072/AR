package com.example.ar.server

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object TeachersTable : Table("teachers") {
    val id = varchar("id", 64)
    val displayName = varchar("display_name", 128)
    val email = varchar("email", 128).uniqueIndex()
    val passwordHash = varchar("password_hash", 256)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val tokenHash = varchar("token_hash", 64)
    val userId = varchar("user_id", 64).index()
    val role = varchar("role", 16)
    val expiresAt = long("expires_at")
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(tokenHash)
}

object GoogleAccountsTable : Table("google_accounts") {
    val googleSubject = varchar("google_subject", 128)
    val email = varchar("email", 128)
    val role = varchar("role", 16)
    val userId = varchar("user_id", 64).uniqueIndex()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(googleSubject)
}

object ClassroomsTable : Table("classrooms") {
    val id = varchar("id", 64)
    val name = varchar("name", 128)
    val grade = varchar("grade", 64)
    val group = varchar("group_name", 64)
    val schoolYear = varchar("school_year", 64)
    val teacherId = varchar("teacher_id", 64).references(TeachersTable.id)
    val joinCode = varchar("join_code", 16).uniqueIndex()
    val joinOpen = bool("join_open").default(true)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object StudentsTable : Table("students") {
    val id = varchar("id", 64)
    val displayName = varchar("display_name", 128)
    val age = integer("age")
    val grade = varchar("grade", 64)
    val avatarId = varchar("avatar_id", 64)
    val consentGiven = bool("consent_given").default(false)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object StudentProgressTable : Table("student_progress") {
    val studentId = varchar("student_id", 64).references(StudentsTable.id)
    val progressJson = text("progress_json")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(studentId)
}

object EnrollmentsTable : Table("enrollments") {
    val classId = varchar("class_id", 64).references(ClassroomsTable.id)
    val studentId = varchar("student_id", 64).references(StudentsTable.id)
    val pinHash = varchar("pin_hash", 256)
    val status = varchar("status", 32) // PENDING, ACTIVE, REJECTED, LEFT
    val joinedAt = long("joined_at")

    override val primaryKey = PrimaryKey(classId, studentId)
}

object RubricsTable : Table("rubrics") {
    val id = varchar("id", 64)
    val classId = varchar("class_id", 64).references(ClassroomsTable.id)
    val stationId = integer("station_id")

    override val primaryKey = PrimaryKey(id)
}

object RubricCriteriaTable : Table("rubric_criteria") {
    val id = varchar("id", 64)
    val rubricId = varchar("rubric_id", 64).references(RubricsTable.id)
    val name = varchar("name", 128)
    val weight = double("weight")

    override val primaryKey = PrimaryKey(id)
}

object RubricLevelsTable : Table("rubric_levels") {
    val id = varchar("id", 64)
    val criterionId = varchar("criterion_id", 64).references(RubricCriteriaTable.id)
    val points = integer("points")
    val label = varchar("label", 64)
    val descriptor = varchar("descriptor", 256)

    override val primaryKey = PrimaryKey(id)
}

object EvaluationRequestsTable : Table("evaluation_requests") {
    val id = varchar("id", 64)
    val studentId = varchar("student_id", 64).references(StudentsTable.id)
    val classId = varchar("class_id", 64).references(ClassroomsTable.id)
    val stationId = integer("station_id")
    val status = varchar("status", 32)
    val requestedAt = long("requested_at")
    val stars = integer("stars").default(0)
    val comment = varchar("comment", 512).default("")

    override val primaryKey = PrimaryKey(id)
}

object EvaluationReviewsTable : Table("evaluation_reviews") {
    val id = varchar("id", 64)
    val requestId = varchar("request_id", 64).references(EvaluationRequestsTable.id)
    val classId = varchar("class_id", 64).references(ClassroomsTable.id)
    val stationId = integer("station_id")
    val totalPercent = double("total_percent")
    val suggestedStars = integer("suggested_stars")
    val finalStars = integer("final_stars")
    val decision = varchar("decision", 32) // PASS, REPEAT
    val bonusAcorns = integer("bonus_acorns").default(0)
    val bonusLeaves = integer("bonus_leaves").default(0)
    val comment = varchar("comment", 512)
    val reviewedAt = long("reviewed_at")
    val reviewedByTeacherId = varchar("reviewed_by_teacher_id", 64)

    override val primaryKey = PrimaryKey(id)
}

object EvaluationScoresTable : Table("evaluation_scores") {
    val reviewId = varchar("review_id", 64).references(EvaluationReviewsTable.id)
    val criterionId = varchar("criterion_id", 64)
    val points = integer("points")

    override val primaryKey = PrimaryKey(reviewId, criterionId)
}

object DbFactory {
    fun init(inMemory: Boolean = false) {
        val dbPath = System.getenv("COGNOKINET_DB_PATH") ?: "./cognokinet.db"
        val dbFile = File(dbPath)
        dbFile.parentFile?.mkdirs()
        if (inMemory && dbFile.exists()) dbFile.delete()
        Database.connect("jdbc:sqlite:${dbFile.absolutePath}", "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(
                GoogleAccountsTable,
                RefreshTokensTable,
                TeachersTable,
                ClassroomsTable,
                StudentsTable,
                EnrollmentsTable,
                StudentProgressTable,
                RubricsTable,
                RubricCriteriaTable,
                RubricLevelsTable,
                EvaluationRequestsTable,
                EvaluationReviewsTable,
                EvaluationScoresTable
            )
        }
    }
}
