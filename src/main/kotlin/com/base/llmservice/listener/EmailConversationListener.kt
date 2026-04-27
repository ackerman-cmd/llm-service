package com.base.llmservice.listener

import com.base.llmservice.event.EmailConversationCreatedEvent
import com.base.llmservice.service.AppealEnrichmentService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Слушает email.conversation.created — срабатывает при каждом новом входящем
 * email-обращении. Запускает LLM-обогащение: приоритет, тема, саммари.
 * Использует отдельную фабрику (без type-headers), аналогично arm-support-service.
 */
@Component
class EmailConversationListener(
    private val objectMapper: ObjectMapper,
    private val enrichmentService: AppealEnrichmentService,
) {
    private val log = LoggerFactory.getLogger(EmailConversationListener::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-conversation-created}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onConversationCreated(payload: String) {
        try {
            val event = objectMapper.readValue(payload, EmailConversationCreatedEvent::class.java)
            log.info("Received email.conversation.created for conversationId={}", event.conversationId)
            enrichmentService.enrich(event)
        } catch (ex: Exception) {
            log.error("Failed to process email.conversation.created: payload={}", payload, ex)
            throw ex
        }
    }
}
