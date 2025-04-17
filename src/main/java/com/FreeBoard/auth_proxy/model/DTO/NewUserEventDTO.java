package com.FreeBoard.auth_proxy.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserEventDTO {

    @NotNull
    @JsonProperty("saga_id")
    private UUID sagaId;

    @NotNull
    @JsonProperty("user_id")
    private UUID userId;

    @NotEmpty
    private String username;

    @NotEmpty
    private String email;
}
