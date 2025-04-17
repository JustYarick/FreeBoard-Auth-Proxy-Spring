package com.FreeBoard.auth_proxy.repository;

import com.FreeBoard.auth_proxy.model.entity.AuthSagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SagaRepository extends JpaRepository<AuthSagaEntity, UUID> {
}