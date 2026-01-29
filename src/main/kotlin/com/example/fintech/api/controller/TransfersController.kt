package com.example.fintech.api.controller

import com.example.fintech.api.dto.CreateOperationResponse
import com.example.fintech.api.dto.CreateTransferRequest
import com.example.fintech.service.TransfersService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transfers")
class TransfersController(
    private val transfersService: TransfersService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun transfer(
        @RequestHeader("Idempotency-Key") idempotencyKey: String,
        @RequestBody @Valid req: CreateTransferRequest
    ): CreateOperationResponse {
        val txn = transfersService.transfer(req.fromAccountId, req.toAccountId, idempotencyKey, req.amount)
        return CreateOperationResponse(txn.id, txn.status)
    }
}
