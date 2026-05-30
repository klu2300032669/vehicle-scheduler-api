package com.affordmed.vehiclescheduler.controller;

import com.affordmed.vehiclescheduler.service.LoggingService;
import com.affordmed.vehiclescheduler.service.VehicleSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    
    @Autowired
    private VehicleSchedulerService schedulerService;
    
    @Autowired
    private LoggingService logger;
    
    @GetMapping("/optimal")
    public ResponseEntity<?> getOptimalSchedule() {
        logger.log("backend", "info", "controller", "GET /api/scheduler/optimal called");
        try {
            Map<String, Object> result = schedulerService.getOptimalSchedule();
            logger.log("backend", "info", "controller", "Successfully returned optimal schedule");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.log("backend", "error", "controller", "Error in /optimal: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "status", "failed"
            ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        logger.log("backend", "debug", "controller", "Health check endpoint called");
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "port", "9093",
            "timestamp", System.currentTimeMillis()
        ));
    }
}