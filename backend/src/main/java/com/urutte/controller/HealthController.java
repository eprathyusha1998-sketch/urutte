package com.urutte.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                health.put("database", "UP");
            } else {
                health.put("database", "DOWN");
                health.put("status", "DOWN");
            }
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Urutte.com");
        info.put("description", "Thread-like Social Media Application");
        info.put("version", "1.0.0");
        info.put("environment", "production");
        return ResponseEntity.ok(info);
    }
}
