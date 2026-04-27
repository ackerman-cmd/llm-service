package com.base.llmservice.service

import com.base.llmservice.client.EmailIntegrationClient
import com.base.llmservice.client.LlmApiClient
import com.base.llmservice.entity.LlmAnalysisLog
import com.base.llmservice.entity.LlmAnalysisStatus
import com.base.llmservice.entity.OutboxEvent
import com.base.llmservice.event.EmailConversationCreatedEvent
import com.base.llmservice.event.LlmAppealEnrichedEvent
import com.base.llmservice.repository.LlmAnalysisLogRepository
import com.base.llmservice.repository.OutboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientException
import java.time.Instant
import java.util.UUID

@Service
class AppealEnrichmentService(
    private val emailIntegrationClient: EmailIntegrationClient,
    private val llmApiClient: LlmApiClient,
    private val analysisLogRepository: LlmAnalysisLogRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(AppealEnrichmentService::class.java)

    @Transactional
    fun enrich(event: EmailConversationCreatedEvent) {
        val conversationId = UUID.fromString(event.conversationId)

        // идемпотентность: пропускаем уже успешно обработанные
        if (analysisLogRepository.existsByConversationIdAndStatus(conversationId, LlmAnalysisStatus.SUCCESS)) {
            log.info("Conversation {} already enriched, skipping", event.conversationId)
            return
        }

        val messages =
            try {
                emailIntegrationClient.getMessages(event.conversationId)
            } catch (ex: RestClientException) {
                log.error("Cannot fetch messages for conversation={}: {}", event.conversationId, ex.message)
                throw ex
            }

        val firstInbound = messages.firstOrNull { it.direction == "INBOUND" } ?: messages.firstOrNull()
        val content =
            firstInbound?.textBody
                ?: firstInbound?.htmlBody?.let { stripHtml(it) }
                ?: ""

        val analysisLog =
            LlmAnalysisLog(
                conversationId = conversationId,
                subject = event.subjectNormalized,
                inputText = content.take(8_000),
                status = LlmAnalysisStatus.PENDING,
            )
        analysisLogRepository.save(analysisLog)

        try {
            val result =
                llmApiClient.analyze(
                    subject = event.subjectNormalized ?: "",
                    content = content,
                    fromEmail = event.fromEmail,
                )

            analysisLog.priority = result.priority
            analysisLog.topicCode = result.topicCode
            analysisLog.summary = result.summary
            analysisLog.status = LlmAnalysisStatus.SUCCESS
            analysisLogRepository.save(analysisLog)

            val enrichedEvent =
                LlmAppealEnrichedEvent(
                    conversationId = event.conversationId,
                    priority = result.priority,
                    topicCode = result.topicCode,
                    summary = result.summary,
                    analyzedAt = Instant.now().toString(),
                )
            outboxEventRepository.save(
                OutboxEvent(
                    aggregateType = "conversation",
                    aggregateId = event.conversationId,
                    eventType = "llm.appeal.enriched",
                    payloadJson = objectMapper.writeValueAsString(enrichedEvent),
                ),
            )

            log.info(
                "Appeal enriched: conversation={} priority={} topicCode={}",
                event.conversationId,
                result.priority,
                result.topicCode,
            )
        } catch (ex: Exception) {
            analysisLog.status = LlmAnalysisStatus.FAILED
            analysisLog.errorMessage = ex.message?.take(1000)
            analysisLogRepository.save(analysisLog)
            log.error("LLM enrichment failed for conversation={}: {}", event.conversationId, ex.message)
            throw ex
        }
    }

    private fun stripHtml(html: String): String =
        html
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
}
