package com.example.saafloop.core.domain

import com.example.saafloop.core.data.CivicAIRepository
import com.example.saafloop.core.model.CivicIntelligenceData
import kotlinx.coroutines.flow.Flow

class GetCivicIntelligenceUseCase(
    private val repository: CivicAIRepository
) {
    operator fun invoke(): Flow<CivicIntelligenceData> {
        return repository.observeCivicIntelligence()
    }
}
