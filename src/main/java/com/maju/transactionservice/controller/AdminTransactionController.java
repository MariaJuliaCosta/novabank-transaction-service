package com.maju.transactionservice.controller;

import com.maju.transactionservice.dto.ApiResponse;
import com.maju.transactionservice.dto.TransactionDTO;
import com.maju.transactionservice.dto.TransactionRequest;
import com.maju.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminTransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionDTO>> deposit(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.deposit(request));
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionByReference(
            @PathVariable String accountNumber
    ) {

        return ResponseEntity.ok(transactionService.getAllTransactionHistoryOfAnAccountNumber(accountNumber));
    }
}
