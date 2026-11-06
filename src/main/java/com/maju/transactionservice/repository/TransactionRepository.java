package com.maju.transactionservice.repository;

import com.maju.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReference(String reference);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccountNumber = :accNum OR t.toAccountNumber = :accNum ORDER BY t.createdAt DESC")
    List<Transaction> findAllByAccountNumber(@Param("accNum") String accountNumber);

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE (t.fromAccountNumber = :accNum OR t.toAccountNumber = :accNum)
          AND t.createdAt BETWEEN :start AND :end
          ORDER BY t.createdAt DESC
    """)
    List<Transaction> findAllAccountNumberAndDateRange(
            @Param("accNum") String accountNumber,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Transaction> findByFromAccountNumber(String fromAccountNumber);
    List<Transaction> findByToAccountNumber(String toAccountNumber);

}
