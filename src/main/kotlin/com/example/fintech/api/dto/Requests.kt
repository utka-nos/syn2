package com.example.fintech.api.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.*

data class TopUpRequest(
    @field:NotNull
    @field:DecimalMin(value = "0.0001", inclusive = true)
    val amount: BigDecimal
)

data class CreateTransferRequest(
    @field:NotNull val fromAccountId: UUID,
    @field:NotNull val toAccountId: UUID,
    @field:NotNull
    @field:DecimalMin(value = "0.0001", inclusive = true)
    val amount: BigDecimal
)
