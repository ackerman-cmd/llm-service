--liquibase formatted sql

--changeset llm-service:002-create-outbox-event
CREATE TABLE llm_service.outbox_event
(
    id             UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload_json   JSONB        NOT NULL,
    status         VARCHAR(50)  NOT NULL             DEFAULT 'PENDING',
    retry_count    INT          NOT NULL             DEFAULT 0,
    next_retry_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL             DEFAULT NOW()
);

CREATE INDEX idx_outbox_event_status_retry
    ON llm_service.outbox_event (status, next_retry_at)
    WHERE status IN ('PENDING', 'FAILED');
