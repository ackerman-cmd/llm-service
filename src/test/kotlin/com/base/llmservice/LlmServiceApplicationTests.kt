package com.base.llmservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["email.conversation.created.test", "llm.appeal.enriched.test"],
)
class LlmServiceApplicationTests {
    companion object {
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> =
            PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
                withDatabaseName("llm_service")
                withUsername("test")
                withPassword("test")
                withInitScript("db/init.sql")
                start()
            }
    }

    @Test
    fun contextLoads() {
    }
}
