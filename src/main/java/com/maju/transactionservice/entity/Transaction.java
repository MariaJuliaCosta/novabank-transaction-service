package com.maju.transactionservice.entity;

import com.maju.transactionservice.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;

    @Column(nullable = false)
    private String fromAccountNumber;

    private String fromBankCode;

    @Column(nullable = false)
    private String toAccountNumber;

    private String toBankCode;

    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    private TransactionDirection transactionDirection;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private LocalDateTime createdAt;
}
