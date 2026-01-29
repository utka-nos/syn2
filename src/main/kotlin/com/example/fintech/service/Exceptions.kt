package com.example.fintech.service

open class NotFoundException(message: String) : RuntimeException(message)
open class ConflictException(message: String) : RuntimeException(message)
open class UnprocessableException(message: String) : RuntimeException(message)
open class BadRequestException(message: String) : RuntimeException(message)
