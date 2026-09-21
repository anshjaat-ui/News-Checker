package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.FactCheckDao
import com.example.data.local.FactCheckEntity
import com.example.data.model.FactCheckResult
import com.example.data.remote.GeminiFactCheckService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FactCheckRepository(
    private val dao: FactCheckDao,
    private val apiService: GeminiFactCheckService = GeminiFactCheckService()
) {

    val history: Flow<List<FactCheckResult>> = dao.getAllFactChecks().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun verifyAndSave(
        query: String,
        bitmap: Bitmap? = null,
        imageUri: String? = null
    ): FactCheckResult {
        val result = apiService.verifyClaim(query, bitmap)
        val resultWithMeta = result.copy(
            originalInput = query,
            imageUri = imageUri,
            timestamp = System.currentTimeMillis()
        )
        val entity = FactCheckEntity.fromDomain(resultWithMeta)
        val savedId = dao.insertFactCheck(entity)
        return resultWithMeta.copy(id = savedId)
    }

    suspend fun deleteCheck(id: Long) {
        dao.deleteFactCheckById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
