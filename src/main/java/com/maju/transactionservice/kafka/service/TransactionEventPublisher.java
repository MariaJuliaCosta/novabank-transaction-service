package com.maju.transactionservice.kafka.service;

import com.maju.transactionservice.kafka.dto.BalanceUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBalanceUpdate(BalanceUpdateEvent event) {
        kafkaTemplate.send("balance-update-events", event.getAccountNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {

                        String topicResult = result.getRecordMetadata().topic();
                        long topicOffset = result.getRecordMetadata().offset();

                        log.info("SENT MESSAGE TO TOPIC {} OFFSET {}", topicResult, topicOffset);
                    }
                });
    }
}
