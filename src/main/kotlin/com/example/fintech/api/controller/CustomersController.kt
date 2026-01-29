package com.example.fintech.api.controller

import com.example.fintech.api.dto.CreateCustomerResponse
import com.example.fintech.service.CustomersService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/customers")
class CustomersController(
    private val customersService: CustomersService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(): CreateCustomerResponse =
        CreateCustomerResponse(customersService.createCustomer())
}
