package com.base.llmservice.config

import com.base.llmservice.config.properties.KafkaTopicsProperties
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
    private val topics: KafkaTopicsProperties,
) {
    @Bean
    fun llmAppealEnrichedTopic(): NewTopic =
        TopicBuilder
            .name(topics.llmAppealEnriched)
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        val props = kafkaProperties.buildProducerProperties(null).toMutableMap<String, Any>()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return KafkaTemplate(DefaultKafkaProducerFactory(props))
    }

    @Bean("emailKafkaListenerContainerFactory")
    fun emailKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.RECORD
        factory.setCommonErrorHandler(
            DefaultErrorHandler(
                { record, ex ->
                    log.warn(
                        "Skipping unprocessable Kafka message topic={} offset={}: {}",
                        record.topic(),
                        record.offset(),
                        ex.message,
                    )
                },
                FixedBackOff(2_000L, 3L),
            ),
        )
        return factory
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(KafkaConfig::class.java)
    }
}
