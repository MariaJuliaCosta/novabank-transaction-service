package com.maju.transactionservice.service.impl;

import com.maju.transactionservice.dto.AccountDTO;
import com.maju.transactionservice.dto.ApiResponse;
import com.maju.transactionservice.dto.TransactionDTO;
import com.maju.transactionservice.dto.TransactionRequest;
import com.maju.transactionservice.entity.Transaction;
import com.maju.transactionservice.enums.*;
import com.maju.transactionservice.exceptions.BadRequestException;
import com.maju.transactionservice.exceptions.NotFoundException;
import com.maju.transactionservice.feign.AccountsFeignClient;
import com.maju.transactionservice.kafka.dto.BalanceUpdateEvent;
import com.maju.transactionservice.kafka.service.TransactionEventPublisher;
import com.maju.transactionservice.repository.TransactionRepository;
import com.maju.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final AccountsFeignClient accountsFeignClient;

    private final ModelMapper modelMapper;

    private final TransactionEventPublisher transactionEventPublisher;

    @Override
    @Transactional
    public ApiResponse<TransactionDTO> deposit(TransactionRequest request) {

        fetchAndValidateAccount(request.getToAccountNumber());

        Transaction deposit = Transaction.builder()
                .reference("DEP"+ UUID.randomUUID().toString().substring(0,8))
                .fromAccountNumber(request.getFromAccountNumber())
                .fromBankCode("NOVA")
                .currency(Currency.USD)
                .toAccountNumber(request.getToAccountNumber())
                .toBankCode("NOVA")
                .amount(request.getAmount())
                .transactionDirection(TransactionDirection.CREDIT)
                .channel(Channel.API)
                .description(request.getDescription())
                .transactionType(TransactionType.DEPOSIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(deposit);

        //notify the account service to update the user account balance
        BalanceUpdateEvent balanceUpdateEvent = BalanceUpdateEvent.builder()
                .accountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .currency(Currency.USD)
                .description(request.getDescription())
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedTransaction.getReference())
                .build();

        transactionEventPublisher.sendBalanceUpdate(balanceUpdateEvent);

        return new ApiResponse<>(
                201,
                "Deposit successfull",
                modelMapper.map(savedTransaction, TransactionDTO.class));
    }

    @Override
    public ApiResponse<TransactionDTO> transfer(TransactionRequest request) {
        if (request.getFromAccountNumber() == null || request.getFromAccountNumber().isEmpty()) {
            throw new BadRequestException("From account number cannot be empty");
        }

        AccountDTO sourceAccount = fetchAndValidateAccount(request.getFromAccountNumber());

        String loggedInUserEmail = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            loggedInUserEmail = authentication.getName();
        }

        log.info("Auth email is: {}", loggedInUserEmail);
        log.info("Account email is: {}", sourceAccount.getOwnerEmail());

        if (!sourceAccount.getOwnerEmail().equals(loggedInUserEmail)) {
            throw new BadRequestException("Access denied: you are not authorized to perform a transfer on behalf of another person");
        }

        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Transaction failed: your account is inactive, please contact customer support");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient funds to transfer on account");
        }

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new BadRequestException("You cannot transfer on your own account");
        }

        //validate the destination bank account
        fetchAndValidateAccount(request.getToAccountNumber());

        Transaction transferInx = Transaction.builder()
                .reference("TRF"+ UUID.randomUUID().toString().substring(0,8))
                .fromAccountNumber(request.getFromAccountNumber())
                .fromBankCode("NOVA")
                .currency(Currency.USD)
                .toAccountNumber(request.getToAccountNumber())
                .toBankCode("NOVA")
                .amount(request.getAmount())
                .channel(Channel.API)
                .description(request.getDescription())
                .transactionType(TransactionType.TRANSFER)
                .transactionStatus(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transferInx);

        //notify account service: to debit the sender
        transactionEventPublisher.sendBalanceUpdate(BalanceUpdateEvent.builder()
                .accountNumber(request.getFromAccountNumber())
                .amount(request.getAmount())
                .currency(Currency.USD)
                .description(request.getDescription())
                .transactionDirection(TransactionDirection.DEBIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedTransaction.getReference())
                .build());

        //notify account service: to credit the receiver
        transactionEventPublisher.sendBalanceUpdate(BalanceUpdateEvent.builder()
                .accountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .currency(Currency.USD)
                .description(request.getDescription())
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedTransaction.getReference())
                .build());

        return new ApiResponse<>(
                201,
                "Transfer success",
                modelMapper.map(savedTransaction, TransactionDTO.class)
        );
    }

    @Override
    public ApiResponse<TransactionDTO> withdraw(TransactionRequest request) {

        AccountDTO account = fetchAndValidateAccount(request.getFromAccountNumber());

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient funds to transfer on account");
        }

        Transaction withdrawInx = Transaction.builder()
                .reference("WID"+ UUID.randomUUID().toString().substring(0,8))
                .fromAccountNumber(request.getFromAccountNumber())
                .fromBankCode("NOVA")
                .currency(Currency.USD)
                .toAccountNumber("VULT")
                .toBankCode("VULT")
                .amount(request.getAmount())
                .channel(Channel.API)
                .description(request.getDescription())
                .transactionType(TransactionType.TRANSFER)
                .transactionStatus(TransactionStatus.SUCCESS)
                .transactionDirection(TransactionDirection.DEBIT)
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedWithdrawInx = transactionRepository.save(withdrawInx);

        //notify account service: to debit the account
        transactionEventPublisher.sendBalanceUpdate(BalanceUpdateEvent.builder()
                .accountNumber(request.getFromAccountNumber())
                .amount(request.getAmount())
                .currency(Currency.USD)
                .description(request.getDescription())
                .transactionDirection(TransactionDirection.DEBIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedWithdrawInx.getReference())
                .build());

        return new ApiResponse<>(
                201,
                "Withdraw success",
                modelMapper.map(savedWithdrawInx, TransactionDTO.class)
        );
    }

    @Override
    public ApiResponse<TransactionDTO> getTransactionByReference(String reference) {

        Transaction txn = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        TransactionDTO dto = modelMapper.map(txn, TransactionDTO.class);

        return new ApiResponse<>(
                201,
                "Transaction success",
                dto
        );
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getAllTransactionHistoryOfAnAccountNumber(String accountNumber) {

        List<Transaction> transactionList = transactionRepository.findAllByAccountNumber(accountNumber);

        List<TransactionDTO> transactionDTOS = transactionList.stream().map(t -> modelMapper.map(t, TransactionDTO.class)).toList();

        return new ApiResponse<>(
                201,
                "Transaction history retrieved successfully",
                transactionDTOS
        );
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getTransactionHistory(String accountNumber, LocalDateTime start, LocalDateTime end) {

        List<Transaction> history = transactionRepository.findAllAccountNumberAndDateRange(accountNumber, start, end);

        List<TransactionDTO> transactionDTOS = history.stream().map(t -> modelMapper.map(t, TransactionDTO.class)).toList();

        return new ApiResponse<>(
                201,
                "Transaction history retrieved successfully",
                transactionDTOS
        );
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getMyTransactionHistoryByDirection(String accountNumber, TransactionDirection direction) {

        List<Transaction> transactions = direction.equals(TransactionDirection.DEBIT) ?
                transactionRepository.findByFromAccountNumber(accountNumber) :
                transactionRepository.findByToAccountNumber(accountNumber);

        List<TransactionDTO> transactionDTOS = transactions.stream().map(t -> modelMapper.map(t, TransactionDTO.class)).toList();

        return new ApiResponse<>(
                201,
                "Transaction history retrieved by direction successfully",
                transactionDTOS
        );
    }


    private AccountDTO fetchAndValidateAccount(String accountNumber) {

        ApiResponse<AccountDTO> response = accountsFeignClient.getAccountByNumber(accountNumber);

        if (response == null || response.data() == null) {
            throw  new NotFoundException("Account " + accountNumber + " not found");
        }

        AccountDTO account = response.data();

        if (account.getAccountStatus().equals(AccountStatus.CLOSED)) {
            throw new BadRequestException("Transaction denied: account is closed");
        }

        return account;
    }
}
