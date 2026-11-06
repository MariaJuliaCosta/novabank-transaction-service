package com.maju.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int statusCode,
        String message,
        T data
) {
}
