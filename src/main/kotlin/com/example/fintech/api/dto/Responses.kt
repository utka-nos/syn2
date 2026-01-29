package com.example.fintech.api.dto

import com.example.fintech.domain.TransactionStatus
import java.util.*

data class CreateOperationResponse(
    val transactionId: UUID,
    val status: TransactionStatus
)
