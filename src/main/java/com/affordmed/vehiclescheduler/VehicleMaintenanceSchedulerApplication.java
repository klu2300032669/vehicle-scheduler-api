package com.affordmed.vehiclescheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VehicleMaintenanceSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleMaintenanceSchedulerApplication.class, args);
        System.out.println("========================================");
        System.out.println("Vehicle Scheduler Application Started!");
        System.out.println("Server running on: http://localhost:9093");
        System.out.println("========================================");
    }
}