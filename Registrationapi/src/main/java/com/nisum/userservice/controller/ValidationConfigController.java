package com.nisum.userservice.controller;

import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.ValidationConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/validation-config")
public class ValidationConfigController {

    private final ValidationConfigRepository validationConfigRepository;

    @Autowired
    public ValidationConfigController(ValidationConfigRepository validationConfigRepository) {
        this.validationConfigRepository = validationConfigRepository;
    }

    @GetMapping
    public List<ValidationConfig> getAllConfigs() {
        return validationConfigRepository.findAll();
    }

    @GetMapping("/{key}")
    public ResponseEntity<ValidationConfig> getConfigByKey(@PathVariable String key) {
        Optional<ValidationConfig> config = validationConfigRepository.findByConfigKey(key);
        return config.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ValidationConfig createConfig(@RequestBody ValidationConfig config) {
        return validationConfigRepository.save(config);
    }

    @PutMapping("/{key}")
    public ResponseEntity<ValidationConfig> updateConfig(@PathVariable String key, @RequestBody ValidationConfig config) {
        Optional<ValidationConfig> existingConfig = validationConfigRepository.findByConfigKey(key);
        if (existingConfig.isPresent()) {
            config.setId(existingConfig.get().getId());
            config.setConfigKey(key);
            return ResponseEntity.ok(validationConfigRepository.save(config));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String key) {
        Optional<ValidationConfig> config = validationConfigRepository.findByConfigKey(key);
        if (config.isPresent()) {
            validationConfigRepository.delete(config.get());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
} 