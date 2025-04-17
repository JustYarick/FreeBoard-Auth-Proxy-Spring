package com.FreeBoard.auth_proxy.model.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCreatedEventDTO {

    @NotNull
    private UUID sagaId;

    @NotNull
    private UUID userId;

    private Instant timestamp;
}
