package com.example.fintech.service

import com.example.fintech.domain.AccountStatus
import com.example.fintech.entity.AccountEntity
import com.example.fintech.repo.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class AccountsService(
    private val customersService: CustomersService,
    private val accounts: AccountRepository
) {
    fun createAccount(customerId: UUID, currency: String): AccountEntity {
        customersService.ensureExists(customerId)
        validateCurrency(currency)

        val entity = AccountEntity(
            customerId = customerId,
            currency = currency.uppercase(),
            status = AccountStatus.ACTIVE,
            balance = BigDecimal.ZERO
        )
        return accounts.save(entity)
    }

    fun getAccount(accountId: UUID): AccountEntity =
        accounts.findById(accountId).orElseThrow { NotFoundException("Account $accountId not found") }

    private fun validateCurrency(currency: String) {
        val c = currency.trim()
        if (c.length != 3 || !c.all { it.isLetter() }) throw BadRequestException("Invalid currency: $currency")
    }
}
