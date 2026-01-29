package com.example.fintech.api.controller

import com.example.fintech.api.dto.CreateAccountRequest
import com.example.fintech.api.dto.TopUpRequest
import com.example.fintech.domain.TransactionType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.not
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.*

class AccountsControllerIT : IntegrationTestBase() {

    @Test
    fun `create and get account`() {
        val customerId = customersService.createCustomer()
        val req = CreateAccountRequest(customerId = customerId, currency = "usd")

        val createResult = mockMvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accountId").isNotEmpty)
            .andExpect(jsonPath("$.customerId").value(customerId.toString()))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.balance").value("0.0000"))
            .andReturn()

        val accountId = UUID.fromString(
            objectMapper.readTree(createResult.response.contentAsString).get("accountId").asText()
        )

        mockMvc.perform(
            get("/accounts/$accountId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accountId").value(accountId.toString()))
            .andExpect(jsonPath("$.customerId").value(customerId.toString()))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.balance").value("0.0000"))
    }

    @Test
    fun `topup creates transaction and appears in list`() {
        val customerId = customersService.createCustomer()
        val account = accountsService.createAccount(customerId, "USD")

        val topUpReq = TopUpRequest(amount = BigDecimal("10.0000"))
        mockMvc.perform(
            post("/accounts/${account.id}/topups")
                .header("Idempotency-Key", "topup-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpReq))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.transactionId").isNotEmpty)
            .andExpect(jsonPath("$.status").value("POSTED"))

        mockMvc.perform(
            get("/accounts/${account.id}/transactions")
                .param("limit", "10")
                .param("offset", "0")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].type").value(TransactionType.TOP_UP.name))
            .andExpect(jsonPath("$.items[0].amount").value("10.0000"))
            .andExpect(jsonPath("$.items[0].currency").value("USD"))
            .andExpect(jsonPath("$.limit").value(10))
            .andExpect(jsonPath("$.offset").value(0))

        val refreshed = accounts.findById(account.id).orElseThrow()
        assertThat(refreshed.balance).isEqualByComparingTo(BigDecimal("10.0000"))
    }



    /////

    private fun createCustomer(): UUID {
        val res = mockMvc.post("/customers") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andReturn().response.contentAsString
        val id = Regex("\"customerId\"\\s*:\\s*\"([^\"]+)\"").find(res)!!.groupValues[1]
        return UUID.fromString(id)
    }

    private fun createAccount(customerId: UUID, currency: String = "USD"): UUID {
        val res = mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"$customerId","currency":"$currency"}"""
        }.andReturn().response.contentAsString
        val id = Regex("\"accountId\"\\s*:\\s*\"([^\"]+)\"").find(res)!!.groupValues[1]
        return UUID.fromString(id)
    }

    @Test
    fun `POST accounts - creates account`() {
        val customerId = createCustomer()

        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"$customerId","currency":"USD"}"""
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.accountId", not(emptyString()))
                jsonPath("$.customerId", `is`(customerId.toString()))
                jsonPath("$.currency", `is`("USD"))
                jsonPath("$.status", `is`("ACTIVE"))
                jsonPath("$.balance", `is`("0.0000"))
            }
    }

    @Test
    fun `POST accounts - unknown customer - 404`() {
        val unknown = UUID.randomUUID()

        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"$unknown","currency":"USD"}"""
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.code", `is`("NOT_FOUND"))
            }
    }

    @Test
    fun `GET accounts - returns account`() {
        val customerId = createCustomer()
        val accountId = createAccount(customerId, "USD")

        mockMvc.get("/accounts/$accountId")
            .andExpect {
                status { isOk() }
                jsonPath("$.accountId", `is`(accountId.toString()))
                jsonPath("$.balance", `is`("0.0000"))
            }
    }

    @Test
    fun `POST topups - increases balance and creates txn`() {
        val customerId = createCustomer()
        val accountId = createAccount(customerId, "USD")

        mockMvc.post("/accounts/$accountId/topups") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "topup-1")
            content = """{"amount": "100.00"}"""
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.transactionId", not(emptyString()))
                jsonPath("$.status", `is`("POSTED"))
            }

        mockMvc.get("/accounts/$accountId")
            .andExpect {
                status { isOk() }
                jsonPath("$.balance", `is`("100.0000"))
            }
    }

    @Test
    fun `POST topups - idempotent repeat returns same transactionId and does not double credit`() {
        val customerId = createCustomer()
        val accountId = createAccount(customerId, "USD")

        val r1 = mockMvc.post("/accounts/$accountId/topups") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "topup-42")
            content = """{"amount": "50.00"}"""
        }.andReturn().response.contentAsString

        val txn1 = Regex("\"transactionId\"\\s*:\\s*\"([^\"]+)\"").find(r1)!!.groupValues[1]

        val r2 = mockMvc.post("/accounts/$accountId/topups") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "topup-42")
            content = """{"amount": "50.00"}"""
        }.andReturn().response.contentAsString

        val txn2 = Regex("\"transactionId\"\\s*:\\s*\"([^\"]+)\"").find(r2)!!.groupValues[1]

        // same txn id
        assert(txn1 == txn2)

        // balance credited only once
        mockMvc.get("/accounts/$accountId")
            .andExpect {
                status { isOk() }
                jsonPath("$.balance", `is`("50.0000"))
            }
    }

    @Test
    fun `GET accounts transactions - returns list containing topup txn`() {
        val customerId = createCustomer()
        val accountId = createAccount(customerId, "USD")

        mockMvc.post("/accounts/$accountId/topups") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "topup-list-1")
            content = """{"amount": "10.00"}"""
        }.andExpect { status { isCreated() } }

        mockMvc.get("/accounts/$accountId/transactions?limit=50&offset=0")
            .andExpect {
                status { isOk() }
                jsonPath("$.items", hasSize<Any>(1))
                jsonPath("$.items[0].type", `is`("TOP_UP"))
                jsonPath("$.items[0].currency", `is`("USD"))
                jsonPath("$.items[0].amount", `is`("10.0000"))
                jsonPath("$.limit", `is`(50))
                jsonPath("$.offset", `is`(0))
            }
    }
}
