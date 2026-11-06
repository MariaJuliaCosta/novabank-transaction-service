package com.maju.transactionservice.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.maju.transactionservice.enums.Currency;
import com.maju.transactionservice.enums.TransactionDirection;
import com.maju.transactionservice.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //ignores files that are null when returning data to the account service
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceUpdateEvent {

    // core transactions used by account service
    private String accountNumber;

    private BigDecimal amount;

    private TransactionDirection transactionDirection; //CREDIT, DEBIT

    private TransactionStatus transactionStatus; //SUCCESS, FAILED etc

    private String reference;

    private Currency currency;

    // core transactions used by notification service
    private String email;

    private String firstName;

    private BigDecimal currentBalance;

    private String description;
}
