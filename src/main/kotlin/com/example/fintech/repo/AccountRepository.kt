package com.example.fintech.repo

import com.example.fintech.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType
import java.util.*

interface AccountRepository : JpaRepository<AccountEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): AccountEntity?

    fun existsByIdAndCustomerId(id: UUID, customerId: UUID): Boolean
}
