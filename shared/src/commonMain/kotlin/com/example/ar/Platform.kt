package com.example.ar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform