package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class FactCheckVerdict(
    val label: String,
    val hindiTitle: String,
    val containerColor: Color,
    val contentColor: Color,
    val badgeColor: Color
) {
    FAKE(
        label = "FAKE",
        hindiTitle = "Jhooth / Nakli",
        containerColor = Color(0xFFFEE2E2),
        contentColor = Color(0xFF991B1B),
        badgeColor = Color(0xFFDC2626)
    ),
    MISLEADING(
        label = "MISLEADING",
        hindiTitle = "Bhramak / Adha Sach",
        containerColor = Color(0xFFFEF3C7),
        contentColor = Color(0xFF92400E),
        badgeColor = Color(0xFFD97706)
    ),
    TRUE(
        label = "TRUE",
        hindiTitle = "Sach / Verified",
        containerColor = Color(0xFFD1FAE5),
        contentColor = Color(0xFF065F46),
        badgeColor = Color(0xFF059669)
    ),
    UNVERIFIED(
        label = "UNVERIFIED",
        hindiTitle = "Apramanit / Source Nahi Mila",
        containerColor = Color(0xFFF1F5F9),
        contentColor = Color(0xFF334155),
        badgeColor = Color(0xFF64748B)
    );

    companion object {
        fun fromString(value: String): FactCheckVerdict {
            val clean = value.trim().uppercase()
            return when {
                clean.contains("FAKE") || clean.contains("FALSE") -> FAKE
                clean.contains("MISLEADING") -> MISLEADING
                clean.contains("TRUE") -> TRUE
                else -> UNVERIFIED
            }
        }
    }
}

data class FactCheckResult(
    val id: Long = 0L,
    val extractedClaim: String,
    val verdict: FactCheckVerdict,
    val confidence: String, // "High", "Medium", "Low"
    val explanation: String,
    val sources: List<String>,
    val userAdvice: String,
    val timestamp: Long = System.currentTimeMillis(),
    val originalInput: String = "",
    val imageUri: String? = null
)
