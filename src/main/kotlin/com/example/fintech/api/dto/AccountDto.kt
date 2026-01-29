package com.example.fintech.api.dto

import com.example.fintech.domain.AccountStatus
import java.util.*

data class CreateAccountRequest(
    val customerId: UUID,
    val currency: String
)

data class AccountResponse(
    val accountId: UUID,
    val customerId: UUID,
    val currency: String,
    val status: AccountStatus,
    val balance: String
)
