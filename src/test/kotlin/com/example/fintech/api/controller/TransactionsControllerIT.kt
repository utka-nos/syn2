package com.example.fintech.api.controller

import com.example.fintech.domain.TransactionType
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

class TransactionsControllerIT : IntegrationTestBase() {

    @Test
    fun `get transaction details`() {
        val customerId = customersService.createCustomer()
        val account = accountsService.createAccount(customerId, "USD")

        val txn = transfersService.topUp(account.id, "topup-details", BigDecimal("12.3400"))

        mockMvc.perform(
            get("/transactions/${txn.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactionId").value(txn.id.toString()))
            .andExpect(jsonPath("$.type").value(TransactionType.TOP_UP.name))
            .andExpect(jsonPath("$.idempotencyKey").value("topup-details"))
            .andExpect(jsonPath("$.fromAccountId").value(nullValue()))
            .andExpect(jsonPath("$.toAccountId").value(account.id.toString()))
            .andExpect(jsonPath("$.amount").value("12.3400"))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.status").value("POSTED"))
    }

    @Test
    fun `GET transaction - unknown id - 404`() {
        mockMvc.get("/transactions/${UUID.randomUUID()}")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.code", `is`("NOT_FOUND"))
            }
    }
}
