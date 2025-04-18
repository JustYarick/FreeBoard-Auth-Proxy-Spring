package com.FreeBoard.auth_proxy.service;

import com.FreeBoard.auth_proxy.model.DTO.NewUserEventDTO;
import org.springframework.kafka.support.SendResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, NewUserEventDTO> newUserKafkaTemplate;

    public CompletableFuture<SendResult<String, NewUserEventDTO>> sendNewUserEvent(NewUserEventDTO event) {
        CompletableFuture<SendResult<String, NewUserEventDTO>> future = newUserKafkaTemplate.send("NewUser", event);

        future.thenAccept(result ->
            log.info("NewUserEvent sent: topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset())
        ).exceptionally(ex -> {
            log.error("Failed to send NewUserEvent", ex);
            return null;
        });

        return future;
    }
 }
