package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FactCheckResult
import com.example.data.model.FactCheckVerdict

@Entity(tableName = "fact_checks")
data class FactCheckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val extractedClaim: String,
    val verdict: String,
    val confidence: String,
    val explanation: String,
    val sources: List<String>,
    val userAdvice: String,
    val timestamp: Long = System.currentTimeMillis(),
    val originalInput: String,
    val imageUri: String? = null
) {
    fun toDomain(): FactCheckResult {
        return FactCheckResult(
            id = id,
            extractedClaim = extractedClaim,
            verdict = FactCheckVerdict.fromString(verdict),
            confidence = confidence,
            explanation = explanation,
            sources = sources,
            userAdvice = userAdvice,
            timestamp = timestamp,
            originalInput = originalInput,
            imageUri = imageUri
        )
    }

    companion object {
        fun fromDomain(domain: FactCheckResult): FactCheckEntity {
            return FactCheckEntity(
                id = domain.id,
                extractedClaim = domain.extractedClaim,
                verdict = domain.verdict.name,
                confidence = domain.confidence,
                explanation = domain.explanation,
                sources = domain.sources,
                userAdvice = domain.userAdvice,
                timestamp = domain.timestamp,
                originalInput = domain.originalInput,
                imageUri = domain.imageUri
            )
        }
    }
}
