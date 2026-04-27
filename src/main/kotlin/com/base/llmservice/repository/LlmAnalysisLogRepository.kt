package com.base.llmservice.repository

import com.base.llmservice.entity.LlmAnalysisLog
import com.base.llmservice.entity.LlmAnalysisStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LlmAnalysisLogRepository : JpaRepository<LlmAnalysisLog, UUID> {
    fun existsByConversationIdAndStatus(
        conversationId: UUID,
        status: LlmAnalysisStatus,
    ): Boolean
}
