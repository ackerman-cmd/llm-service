package com.base.llmservice.repository

import com.base.llmservice.entity.OutboxEvent
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    @Query(
        """
        SELECT e FROM OutboxEvent e
        WHERE e.status IN ('PENDING', 'FAILED')
          AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
        ORDER BY e.createdAt ASC
        """,
    )
    fun findReadyToPublish(
        now: Instant,
        pageable: Pageable,
    ): List<OutboxEvent>
}
