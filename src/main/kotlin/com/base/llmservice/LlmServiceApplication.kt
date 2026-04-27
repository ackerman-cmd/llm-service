package com.base.llmservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LlmServiceApplication

fun main(args: Array<String>) {
    runApplication<LlmServiceApplication>(*args)
}
