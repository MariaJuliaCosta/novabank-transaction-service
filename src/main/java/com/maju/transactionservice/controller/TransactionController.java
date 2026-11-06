package com.maju.transactionservice.controller;

import com.maju.transactionservice.dto.ApiResponse;
import com.maju.transactionservice.dto.TransactionDTO;
import com.maju.transactionservice.dto.TransactionRequest;
import com.maju.transactionservice.enums.TransactionDirection;
import com.maju.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDTO>> transfer(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionDTO>> withdraw(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.withdraw(request));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getHistory(
            @RequestParam String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate end
            ) {

        //convert the start date to local date time if it provided else assign January 2026 as the start day time
        LocalDateTime startDate = (start != null)
                ? start.atStartOfDay()
                : LocalDateTime.of(2026, 1, 1, 0, 0);

        LocalDateTime endDate = (end != null)
                ? end.atTime(LocalTime.MAX)
                : LocalDateTime.now();

        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber, startDate, endDate));
    }

    @GetMapping("/history/direction")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getHistoryByDirection(
            @RequestParam String accountNumber,
            @RequestParam TransactionDirection direction
    ) {

        return ResponseEntity.ok(transactionService.getMyTransactionHistoryByDirection(accountNumber, direction));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionByReference(
            @PathVariable String reference
    ) {

        return ResponseEntity.ok(transactionService.getTransactionByReference(reference));
    }
}
