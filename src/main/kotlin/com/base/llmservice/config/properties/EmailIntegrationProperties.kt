package com.base.llmservice.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.email-integration")
data class EmailIntegrationProperties(
    var baseUrl: String = "http://localhost:8084",
    var connectTimeoutMillis: Long = 5_000,
    var readTimeoutMillis: Long = 30_000,
)
