package com.base.llmservice.client.dto

data class EmailMessageResponse(
    val id: String,
    val direction: String,
    val status: String,
    val fromEmail: String,
    val fromName: String?,
    val subject: String?,
    val textBody: String?,
    val htmlBody: String?,
    val sentAt: String?,
    val receivedAt: String?,
)
