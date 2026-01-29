package com.example.fintech

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FintechWalletApplication

fun main(args: Array<String>) {
    runApplication<FintechWalletApplication>(*args)
}