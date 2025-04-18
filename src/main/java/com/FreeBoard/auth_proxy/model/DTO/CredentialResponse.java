package com.FreeBoard.auth_proxy.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CredentialResponse {

    public CredentialResponse(String data, String type) {
        this.type = type;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    @JsonProperty("type")
    private String type;
    @JsonProperty("data")
    private String data;
    private LocalDateTime timestamp;
}