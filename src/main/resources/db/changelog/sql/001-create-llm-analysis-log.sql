--liquibase formatted sql

--changeset llm-service:001-create-llm-analysis-log
CREATE TABLE llm_service.llm_analysis_log
(
    id              UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL,
    subject         TEXT,
    input_text      TEXT,
    priority        VARCHAR(16),
    topic_code      VARCHAR(64),
    summary         TEXT,
    status          VARCHAR(16) NOT NULL             DEFAULT 'PENDING',
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL             DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL             DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_llm_analysis_log_conversation
    ON llm_service.llm_analysis_log (conversation_id)
    WHERE status = 'SUCCESS';

CREATE INDEX idx_llm_analysis_log_status ON llm_service.llm_analysis_log (status);
