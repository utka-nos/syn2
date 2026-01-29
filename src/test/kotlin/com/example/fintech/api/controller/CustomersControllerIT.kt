package com.example.fintech.api.controller

import com.example.fintech.api.dto.CreateCustomerResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CustomersControllerIT : IntegrationTestBase() {

    @Test
    fun `create customer returns id and persists`() {
        val result = mockMvc.perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.customerId").isNotEmpty)
            .andReturn()

        val body = result.response.contentAsString
        val response = objectMapper.readValue(body, CreateCustomerResponse::class.java)

        assertThat(customers.existsById(response.customerId)).isTrue
    }
}
