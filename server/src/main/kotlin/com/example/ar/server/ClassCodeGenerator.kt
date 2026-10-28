package com.example.ar.server

import java.security.SecureRandom

object ClassCodeGenerator {
    // Alphabet excluding confusing characters O, 0, I, 1, L
    private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    private val random = SecureRandom()

    fun generateCode(length: Int = 6): String {
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            val idx = random.nextInt(ALPHABET.length)
            sb.append(ALPHABET[idx])
        }
        return sb.toString()
    }
}
