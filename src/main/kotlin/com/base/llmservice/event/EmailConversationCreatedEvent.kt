package com.base.llmservice.event

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: email.conversation.created — дублирует DTO из email-integration-service */
data class EmailConversationCreatedEvent(
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("mailbox_id") val mailboxId: String,
    @JsonProperty("first_message_id") val firstMessageId: String,
    @JsonProperty("from_email") val fromEmail: String,
    @JsonProperty("subject_normalized") val subjectNormalized: String?,
    @JsonProperty("created_at") val createdAt: String,
)
