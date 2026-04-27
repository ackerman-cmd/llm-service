package com.base.llmservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "llm_analysis_log", schema = "llm_service")
class LlmAnalysisLog(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "conversation_id", nullable = false)
    val conversationId: UUID,
    @Column(columnDefinition = "TEXT")
    val subject: String?,
    @Column(name = "input_text", columnDefinition = "TEXT")
    val inputText: String?,
    @Column(length = 16)
    var priority: String? = null,
    @Column(name = "topic_code", length = 64)
    var topicCode: String? = null,
    @Column(columnDefinition = "TEXT")
    var summary: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    var status: LlmAnalysisStatus = LlmAnalysisStatus.PENDING,
    @Column(name = "error_message", columnDefinition = "TEXT")
    var errorMessage: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}

enum class LlmAnalysisStatus { PENDING, SUCCESS, FAILED }
