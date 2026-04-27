package com.base.llmservice.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.llm")
data class LlmApiProperties(
    var baseUrl: String = "https://vsellm.ru",
    var apiKey: String = "",
    var model: String = "gpt-4o-mini",
    var connectTimeoutMillis: Long = 10_000,
    var readTimeoutMillis: Long = 60_000,
)
