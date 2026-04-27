package com.base.llmservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox_event", schema = "llm_service")
class OutboxEvent(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "aggregate_type", nullable = false, length = 100)
    val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false, length = 255)
    val aggregateId: String,
    /** Используется как имя Kafka-топика при публикации. */
    @Column(name = "event_type", nullable = false, length = 100)
    val eventType: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "JSONB")
    val payloadJson: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: OutboxEventStatus = OutboxEventStatus.PENDING,
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,
    @Column(name = "next_retry_at")
    var nextRetryAt: Instant? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)

enum class OutboxEventStatus { PENDING, PUBLISHED, FAILED }
