package com.example.fintech.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "customer")
class CustomerEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)