package com.base.llmservice.client

import com.base.llmservice.client.dto.LlmAnalysisResult
import com.base.llmservice.client.dto.LlmChatRequest
import com.base.llmservice.client.dto.LlmChatResponse
import com.base.llmservice.config.properties.LlmApiProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class LlmApiClient(
    @Qualifier("llmApiRestClient") private val restClient: RestClient,
    private val props: LlmApiProperties,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(LlmApiClient::class.java)

    companion object {
        private val VALID_PRIORITIES = setOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
        private val VALID_TOPIC_CODES =
            setOf(
                "CARD_BLOCK",
                "CARD_ISSUE",
                "CARD_LIMITS",
                "PAYMENT_ERROR",
                "TRANSFER_ISSUE",
                "IB_ACCESS",
                "IB_FUNCTIONALITY",
                "MOBILE_APP",
                "LOAN_PAYMENT",
                "LOAN_RESTRUCTURING",
                "ACCOUNT_STATEMENT",
                "ACCOUNT_CLOSE",
                "FRAUD",
                "PHISHING",
                "DATA_CHANGE",
                "GENERAL_QUESTION",
                "COMPLAINT",
                "TECHNICAL_ISSUE",
            )

        private fun buildSystemPrompt() =
            """
            Ты — специалист по категоризации обращений в службу поддержки банка.
            Проанализируй обращение клиента и верни JSON со следующими полями:
            - "priority": приоритет обращения, одно из: LOW, MEDIUM, HIGH, CRITICAL
            - "topic_code": код темы, одно из: CARD_BLOCK, CARD_ISSUE, CARD_LIMITS,
              PAYMENT_ERROR, TRANSFER_ISSUE, IB_ACCESS, IB_FUNCTIONALITY, MOBILE_APP,
              LOAN_PAYMENT, LOAN_RESTRUCTURING, ACCOUNT_STATEMENT, ACCOUNT_CLOSE,
              FRAUD, PHISHING, DATA_CHANGE, GENERAL_QUESTION, COMPLAINT, TECHNICAL_ISSUE
            - "summary": краткое резюме обращения на русском языке, 1–2 предложения

            Правила выбора приоритета:
            CRITICAL — мошенничество, кража средств, взлом аккаунта, угроза безопасности
            HIGH     — блокировка карты, ошибка платежа, невозможность войти в интернет-банк
            MEDIUM   — вопросы по лимитам, оформление продуктов, технические сбои без потерь
            LOW      — общие вопросы, пожелания, несрочные консультации

            Отвечай строго в формате JSON, без пояснений и без оберток типа ```json.
            """.trimIndent()
    }

    fun analyze(
        subject: String,
        content: String,
        fromEmail: String,
    ): LlmAnalysisResult {
        val userMessage =
            buildString {
                append("Тема: ").appendLine(subject.ifBlank { "(не указана)" })
                append("От: ").appendLine(fromEmail)
                append("Содержание:\n").append(content.take(4000))
            }

        val request =
            LlmChatRequest(
                model = props.model,
                messages =
                    listOf(
                        LlmChatRequest.LlmMessage("system", buildSystemPrompt()),
                        LlmChatRequest.LlmMessage("user", userMessage),
                    ),
            )

        log.debug("Sending LLM analysis request, subject={}", subject)

        val response =
            try {
                restClient
                    .post()
                    .uri("/v1/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(LlmChatResponse::class.java)
                    ?: throw IllegalStateException("Empty response from LLM API")
            } catch (ex: RestClientResponseException) {
                log.error("LLM API error: status={} body={}", ex.statusCode, ex.responseBodyAsString)
                throw ex
            }

        val rawJson =
            response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?: throw IllegalStateException("LLM returned no content")

        log.debug("LLM raw response: {}", rawJson)

        val result = objectMapper.readValue(rawJson, LlmAnalysisResult::class.java)
        return result.copy(
            priority = result.priority.uppercase().let { if (it in VALID_PRIORITIES) it else "MEDIUM" },
            topicCode = result.topicCode?.uppercase()?.let { if (it in VALID_TOPIC_CODES) it else null },
        )
    }
}
