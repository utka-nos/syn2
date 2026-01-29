package com.example.fintech.api.error

import com.example.fintech.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun notFound(e: NotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError("NOT_FOUND", e.message ?: "Not found"))

    @ExceptionHandler(ConflictException::class)
    fun conflict(e: ConflictException) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError("CONFLICT", e.message ?: "Conflict"))

    @ExceptionHandler(UnprocessableException::class)
    fun unprocessable(e: UnprocessableException) =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiError("UNPROCESSABLE_ENTITY", e.message ?: "Unprocessable"))

    @ExceptionHandler(BadRequestException::class)
    fun badRequest(e: BadRequestException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("BAD_REQUEST", e.message ?: "Bad request"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val msg = e.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("VALIDATION_ERROR", msg))
    }

    @ExceptionHandler(Exception::class)
    fun unknown(e: Exception): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("INTERNAL_ERROR", "Unexpected error"))
    }
}
