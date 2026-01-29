package com.example.fintech.service

import com.example.fintech.entity.CustomerEntity
import com.example.fintech.repo.CustomerRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomersService(
    private val customers: CustomerRepository
) {
    fun createCustomer(): UUID {
        val saved = customers.save(CustomerEntity())
        return saved.id
    }

    fun ensureExists(customerId: UUID) {
        if (!customers.existsById(customerId)) {
            throw NotFoundException("Customer $customerId not found")
        }
    }
}
