package com.example.fintech.api.controller

import com.example.fintech.api.dto.CreateTransferRequest
import com.example.fintech.domain.TransactionStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.not
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

class TransfersControllerIT : IntegrationTestBase() {

    @Test
    fun `transfer moves funds between accounts`() {
        val customerId = customersService.createCustomer()
        val from = accountsService.createAccount(customerId, "USD")
        val to = accountsService.createAccount(customerId, "USD")

        transfersService.topUp(from.id, "seed-topup", BigDecimal("100.0000"))

        val req = CreateTransferRequest(fromAccountId = from.id, toAccountId = to.id, amount = BigDecimal("25.0000"))
        mockMvc.perform(
            post("/transfers")
                .header("Idempotency-Key", "transfer-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.transactionId").isNotEmpty)
            .andExpect(jsonPath("$.status").value(TransactionStatus.POSTED.name))

        val fromAfter = accounts.findById(from.id).orElseThrow()
        val toAfter = accounts.findById(to.id).orElseThrow()

        assertThat(fromAfter.balance).isEqualByComparingTo(BigDecimal("75.0000"))
        assertThat(toAfter.balance).isEqualByComparingTo(BigDecimal("25.0000"))
    }

    @Test
    fun `transfer with same account returns bad request`() {
        val customerId = customersService.createCustomer()
        val acc = accountsService.createAccount(customerId, "USD")

        val req = CreateTransferRequest(fromAccountId = acc.id, toAccountId = acc.id, amount = BigDecimal("1.0000"))
        mockMvc.perform(
            post("/transfers")
                .header("Idempotency-Key", "transfer-err")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
    }

    ////

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

    private fun topUp(accountId: UUID, key: String, amount: String) {
        mockMvc.post("/accounts/$accountId/topups") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", key)
            content = """{"amount":"$amount"}"""
        }.andExpect { status { isCreated() } }
    }

    @Test
    fun `POST transfers - moves money between accounts`() {
        val customerId = createCustomer()
        val from = createAccount(customerId, "USD")
        val to = createAccount(customerId, "USD")

        topUp(from, "seed-1", "100.00")

        mockMvc.post("/transfers") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "tr-1")
            content = """{"fromAccountId":"$from","toAccountId":"$to","amount":"10.50"}"""
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.transactionId", not(emptyString()))
                jsonPath("$.status", `is`("POSTED"))
            }

        mockMvc.get("/accounts/$from").andExpect {
            status { isOk() }
            jsonPath("$.balance", `is`("89.5000"))
        }

        mockMvc.get("/accounts/$to").andExpect {
            status { isOk() }
            jsonPath("$.balance", `is`("10.5000"))
        }
    }

    @Test
    fun `POST transfers - insufficient funds - 422`() {
        val customerId = createCustomer()
        val from = createAccount(customerId, "USD")
        val to = createAccount(customerId, "USD")

        mockMvc.post("/transfers") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "tr-nsf")
            content = """{"fromAccountId":"$from","toAccountId":"$to","amount":"1.00"}"""
        }
            .andExpect {
                status { isUnprocessableEntity() }
                jsonPath("$.code", `is`("UNPROCESSABLE_ENTITY"))
            }
    }

    @Test
    fun `POST transfers - currency mismatch - 400`() {
        val customerId = createCustomer()
        val from = createAccount(customerId, "USD")
        val to = createAccount(customerId, "EUR")
        topUp(from, "seed-2", "20.00")

        mockMvc.post("/transfers") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "tr-cur")
            content = """{"fromAccountId":"$from","toAccountId":"$to","amount":"1.00"}"""
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code", `is`("BAD_REQUEST"))
            }
    }

    @Test
    fun `POST transfers - idempotent repeat returns same transactionId and does not double move`() {
        val customerId = createCustomer()
        val from = createAccount(customerId, "USD")
        val to = createAccount(customerId, "USD")
        topUp(from, "seed-3", "50.00")

        val r1 = mockMvc.post("/transfers") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "tr-42")
            content = """{"fromAccountId":"$from","toAccountId":"$to","amount":"10.00"}"""
        }.andReturn().response.contentAsString
        val txn1 = Regex("\"transactionId\"\\s*:\\s*\"([^\"]+)\"").find(r1)!!.groupValues[1]

        val r2 = mockMvc.post("/transfers") {
            contentType = MediaType.APPLICATION_JSON
            header("Idempotency-Key", "tr-42")
            content = """{"fromAccountId":"$from","toAccountId":"$to","amount":"10.00"}"""
        }.andReturn().response.contentAsString
        val txn2 = Regex("\"transactionId\"\\s*:\\s*\"([^\"]+)\"").find(r2)!!.groupValues[1]

        assert(txn1 == txn2)

        mockMvc.get("/accounts/$from").andExpect { jsonPath("$.balance", `is`("40.0000")) }
        mockMvc.get("/accounts/$to").andExpect { jsonPath("$.balance", `is`("10.0000")) }
    }
}
