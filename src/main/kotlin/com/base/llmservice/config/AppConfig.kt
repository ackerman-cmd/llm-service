package com.base.llmservice.config

import com.base.llmservice.config.properties.EmailIntegrationProperties
import com.base.llmservice.config.properties.LlmApiProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class AppConfig {
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Bean("emailIntegrationRestClient")
    fun emailIntegrationRestClient(props: EmailIntegrationProperties): RestClient {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(props.connectTimeoutMillis.toInt())
                setReadTimeout(props.readTimeoutMillis.toInt())
            }
        return RestClient
            .builder()
            .baseUrl(props.baseUrl.trimEnd('/'))
            .requestFactory(factory)
            .build()
    }

    @Bean("llmApiRestClient")
    fun llmApiRestClient(props: LlmApiProperties): RestClient {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(props.connectTimeoutMillis.toInt())
                setReadTimeout(props.readTimeoutMillis.toInt())
            }
        return RestClient
            .builder()
            .baseUrl(props.baseUrl.trimEnd('/'))
            .defaultHeader("Authorization", "Bearer ${props.apiKey}")
            .defaultHeader("Content-Type", "application/json")
            .requestFactory(factory)
            .build()
    }
}
