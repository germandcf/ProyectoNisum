package com.nisum.userservice.repository;

import com.nisum.userservice.model.ValidationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidationConfigRepository extends JpaRepository<ValidationConfig, String> {
    Optional<ValidationConfig> findByConfigKey(String configKey);
} 