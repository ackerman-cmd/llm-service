package com.base.llmservice.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LlmChatRequest(
    @JsonProperty("model") val model: String,
    @JsonProperty("messages") val messages: List<LlmMessage>,
    @JsonProperty("temperature") val temperature: Double = 0.1,
    @JsonProperty("response_format") val responseFormat: ResponseFormat = ResponseFormat(),
) {
    data class LlmMessage(
        @JsonProperty("role") val role: String,
        @JsonProperty("content") val content: String,
    )

    data class ResponseFormat(
        @JsonProperty("type") val type: String = "json_object",
    )
}
