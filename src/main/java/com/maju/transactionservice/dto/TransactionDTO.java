package com.maju.transactionservice.dto;

import com.maju.transactionservice.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDTO {

    private Long id;

    private String reference;

    private String fromAccountNumber;

    private String fromBankCode;

    private String toAccountNumber;

    private String toBankCode;

    private BigDecimal amount;

    private String description;

    private Currency currency;

    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

    private TransactionDirection transactionDirection;

    private Channel channel;

    private LocalDateTime createdAt;
}
