package com.base.llmservice.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EmailMessageResponse(
    @JsonProperty("id") val id: String,
    @JsonProperty("direction") val direction: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("from_email") val fromEmail: String,
    @JsonProperty("from_name") val fromName: String?,
    @JsonProperty("subject") val subject: String?,
    @JsonProperty("text_body") val textBody: String?,
    @JsonProperty("html_body") val htmlBody: String?,
    @JsonProperty("sent_at") val sentAt: String?,
    @JsonProperty("received_at") val receivedAt: String?,
)
