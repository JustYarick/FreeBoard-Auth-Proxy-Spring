package com.FreeBoard.auth_proxy.controller.Kafka;

import com.FreeBoard.auth_proxy.model.DTO.ProfileCreatedEventDTO;
import com.FreeBoard.auth_proxy.model.DTO.ProfileFailedEventDTO;
import com.FreeBoard.auth_proxy.model.SagaStatus;
import com.FreeBoard.auth_proxy.service.AuthSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListeners {

    private final AuthSagaService authSagaService;

    @KafkaListener(topics = "profileCreated", groupId = "Users")
    void profileCreated(ProfileCreatedEventDTO profileCreatedEventDTO) {
        authSagaService.markAsProfileCreated(profileCreatedEventDTO.getSagaId());

        authSagaService.updateStatuses(profileCreatedEventDTO.getSagaId(), SagaStatus.COMPLETED);
    }

    @KafkaListener(topics = "profileFailed", groupId = "Users")
    void profileFailed(ProfileFailedEventDTO profileFailedEventDTO) {
        authSagaService.markAsFailed(profileFailedEventDTO.getSagaId(), "Profile creation failed");
    }
}