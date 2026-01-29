package com.example.fintech.api.controller

import com.example.fintech.repo.AccountRepository
import com.example.fintech.repo.CustomerRepository
import com.example.fintech.repo.TransactionRepository
import com.example.fintech.service.AccountsService
import com.example.fintech.service.CustomersService
import com.example.fintech.service.TransfersService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var customersService: CustomersService

    @Autowired
    lateinit var accountsService: AccountsService

    @Autowired
    lateinit var transfersService: TransfersService

    @Autowired
    lateinit var customers: CustomerRepository

    @Autowired
    lateinit var accounts: AccountRepository

    @Autowired
    lateinit var transactions: TransactionRepository

    @BeforeEach
    fun cleanup() {
        transactions.deleteAll()
        accounts.deleteAll()
        customers.deleteAll()
    }
}
