package com.base.llmservice.client

import com.base.llmservice.client.dto.EmailMessageResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class EmailIntegrationClient(
    @Qualifier("emailIntegrationRestClient") private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(EmailIntegrationClient::class.java)

    fun getMessages(conversationId: String): List<EmailMessageResponse> =
        try {
            restClient
                .get()
                .uri("/internal/conversations/{id}/messages", conversationId)
                .retrieve()
                .body(object : ParameterizedTypeReference<List<EmailMessageResponse>>() {})
                ?: emptyList()
        } catch (ex: RestClientResponseException) {
            log.error(
                "Failed to fetch messages for conversation={}: status={} body={}",
                conversationId,
                ex.statusCode,
                ex.responseBodyAsString,
            )
            throw ex
        }
}
