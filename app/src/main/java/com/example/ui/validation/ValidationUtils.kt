package com.example.ui.validation

import androidx.compose.ui.graphics.Color

data class PasswordStrength(
    val score: Int, // 0 to 4
    val label: String,
    val color: Color,
    val percentage: Float, // 0.0f to 1.0f
    val hasMinLength: Boolean, // >= 8
    val hasUppercase: Boolean, // [A-Z]
    val hasLowercase: Boolean, // [a-z]
    val hasDigit: Boolean,     // [0-9]
    val hasSpecialChar: Boolean // [!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]
)

object ValidationUtils {
    // Standard email pattern
    private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$")

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidPhone(phone: String): Boolean {
        val digitsOnly = phone.filter { it.isDigit() }
        return digitsOnly.length in 7..15
    }

    fun isValidEmailOrPhone(input: String): Boolean {
        if (input.isBlank()) return false
        return isValidEmail(input) || isValidPhone(input)
    }

    fun evaluatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(
                score = 0,
                label = "Enter password",
                color = Color(0xFF9CA3AF),
                percentage = 0f,
                hasMinLength = false,
                hasUppercase = false,
                hasLowercase = false,
                hasDigit = false,
                hasSpecialChar = false
            )
        }

        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        var metCriteriaCount = 0
        if (hasMinLength) metCriteriaCount++
        if (hasUppercase) metCriteriaCount++
        if (hasLowercase) metCriteriaCount++
        if (hasDigit) metCriteriaCount++
        if (hasSpecialChar) metCriteriaCount++

        val (label, color, score, percentage) = when {
            password.length < 6 -> Quadruple("Too Short", Color(0xFFEF4444), 1, 0.2f)
            metCriteriaCount <= 2 -> Quadruple("Weak", Color(0xFFF97316), 1, 0.35f)
            metCriteriaCount == 3 -> Quadruple("Fair", Color(0xFFEAB308), 2, 0.60f)
            metCriteriaCount == 4 -> Quadruple("Good", Color(0xFF10B981), 3, 0.82f)
            else -> Quadruple("Strong", Color(0xFF059669), 4, 1.0f)
        }

        return PasswordStrength(
            score = score,
            label = label,
            color = color,
            percentage = percentage,
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecialChar
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
