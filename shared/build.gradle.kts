import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

abstract class GenerateApiConfigTask : DefaultTask() {
    @get:Input
    abstract val apiBaseUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val escapedUrl = apiBaseUrl.get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val file = outputDirectory.file("com/example/ar/cognokinet/ApiConfig.kt").get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """package com.example.ar.cognokinet

internal const val API_BASE_URL = "$escapedUrl"
""".trimIndent()
        )
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("org.jetbrains.kotlin.plugin.serialization")
}

val apiBaseUrlProvider = providers.gradleProperty("cognokinetApiBaseUrl")
    .orElse(providers.environmentVariable("COGNOKINET_API_BASE_URL"))
    .orElse("http://192.168.100.33:8080")
val generatedApiConfigDir = layout.buildDirectory.dir("generated/api-config/commonMain")
val generateApiConfig = tasks.register<GenerateApiConfigTask>("generateApiConfig") {
    apiBaseUrl.set(apiBaseUrlProvider)
    outputDirectory.set(generatedApiConfigDir)
}
tasks.configureEach {
    if (name.startsWith("compile") && (name.contains("Kotlin") || name.endsWith("AndroidMain"))) {
        dependsOn(generateApiConfig)
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    android {
       namespace = "com.example.ar.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            api(libs.sceneview)
            api(libs.arsceneview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.credentialManager)
            implementation(libs.credentialManagerPlayServices)
            implementation(libs.googleId)
            implementation(libs.ktorClientAndroid)
        }
        iosMain.dependencies {
            implementation(libs.ktorClientDarwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktorClientCio)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktorClientCore)
            implementation(libs.ktorClientContentNegotiation)
            implementation(libs.ktorSerializationJson)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(generatedApiConfigDir)

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
