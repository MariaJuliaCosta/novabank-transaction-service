package com.maju.transactionservice.service;

import com.maju.transactionservice.dto.ApiResponse;
import com.maju.transactionservice.dto.TransactionDTO;
import com.maju.transactionservice.dto.TransactionRequest;
import com.maju.transactionservice.enums.TransactionDirection;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    ApiResponse<TransactionDTO> deposit(TransactionRequest request);

    ApiResponse<TransactionDTO> transfer(TransactionRequest request);

    ApiResponse<TransactionDTO> withdraw(TransactionRequest request);

    ApiResponse<TransactionDTO> getTransactionByReference(String reference);

    ApiResponse<List<TransactionDTO>> getAllTransactionHistoryOfAnAccountNumber(String accountNumber);

    ApiResponse<List<TransactionDTO>> getTransactionHistory(String accountNumber, LocalDateTime start, LocalDateTime end);

    ApiResponse<List<TransactionDTO>> getMyTransactionHistoryByDirection(String accountNumber, TransactionDirection direction);
}
