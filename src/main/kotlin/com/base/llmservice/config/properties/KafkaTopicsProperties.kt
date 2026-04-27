package com.base.llmservice.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    var emailConversationCreated: String = "email.conversation.created",
    var llmAppealEnriched: String = "llm.appeal.enriched",
)
