package com.base.llmservice.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LlmChatResponse(
    @JsonProperty("id") val id: String?,
    @JsonProperty("choices") val choices: List<Choice>,
) {
    data class Choice(
        @JsonProperty("message") val message: Message,
        @JsonProperty("finish_reason") val finishReason: String?,
    )

    data class Message(
        @JsonProperty("role") val role: String,
        @JsonProperty("content") val content: String,
    )
}

data class LlmAnalysisResult(
    @JsonProperty("priority") val priority: String,
    @JsonProperty("topic_code") val topicCode: String?,
    @JsonProperty("summary") val summary: String,
)
