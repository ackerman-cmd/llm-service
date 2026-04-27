package com.base.llmservice.outbox

import com.base.llmservice.entity.OutboxEvent
import com.base.llmservice.entity.OutboxEventStatus
import com.base.llmservice.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class OutboxScheduler(
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val log = LoggerFactory.getLogger(OutboxScheduler::class.java)
    private val batchSize = 50
    private val maxRetries = 5

    @Scheduled(fixedDelay = 5_000)
    @Transactional
    fun processOutboxEvents() {
        val events = outboxEventRepository.findReadyToPublish(Instant.now(), PageRequest.of(0, batchSize))
        if (events.isEmpty()) return

        for (event in events) {
            tryPublish(event)
        }
    }

    private fun tryPublish(event: OutboxEvent) {
        try {
            kafkaTemplate
                .send(event.eventType, event.aggregateId, event.payloadJson)
                .get(10, TimeUnit.SECONDS)
            event.status = OutboxEventStatus.PUBLISHED
            log.debug("Published outbox event id={} topic={}", event.id, event.eventType)
        } catch (ex: Exception) {
            log.error("Failed to publish outbox event id={} topic={}: {}", event.id, event.eventType, ex.message)
            event.retryCount++
            if (event.retryCount >= maxRetries) {
                event.status = OutboxEventStatus.FAILED
                log.error("Outbox event id={} exceeded max retries, marking FAILED", event.id)
            } else {
                val backoffSeconds = (1L shl event.retryCount) * 10L
                event.nextRetryAt = Instant.now().plusSeconds(backoffSeconds)
            }
        }
    }
}
