package com.example.fintech.repo

import com.example.fintech.entity.CustomerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CustomerRepository : JpaRepository<CustomerEntity, UUID>
