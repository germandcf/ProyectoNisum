package com.nisum.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db")
    public ResponseEntity<String> checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return ResponseEntity.ok("Database connection is OK");
            } else {
                return ResponseEntity.internalServerError().body("Database connection is not valid");
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body("Database connection error: " + e.getMessage());
        }
    }
} 