package com.affordmed.vehiclescheduler.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingService {
    
    @Value("${test.server.url}")
    private String serverUrl;
    
    @Value("${access.token}")
    private String accessToken;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void log(String stack, String level, String packageName, String message) {
        Map<String, String> logBody = new HashMap<>();
        logBody.put("stack", stack);
        logBody.put("level", level);
        logBody.put("package", packageName);
        logBody.put("message", message);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(logBody, headers);
            restTemplate.postForEntity(serverUrl + "/logs", entity, String.class);
            
            System.out.println("✓ LOG SENT: [" + level.toUpperCase() + "] " + packageName + " - " + message);
        } catch (Exception e) {
            System.err.println("✗ LOG FAILED: " + e.getMessage());
        }
    }
}