package com.affordmed.vehiclescheduler.service;

import com.affordmed.vehiclescheduler.model.*;
import com.affordmed.vehiclescheduler.utils.KnapsackSolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class VehicleSchedulerService {
    
    @Autowired
    private LoggingService logger;
    
    @Value("${test.server.url}")
    private String serverUrl;
    
    @Value("${access.token}")
    private String accessToken;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public Map<String, Object> getOptimalSchedule() {
        logger.log("backend", "info", "service", "Starting vehicle scheduling optimization");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            
            List<Depot> depots = fetchDepots();
            System.out.println("Depots received: " + depots.size());
            
            if (depots.isEmpty()) {
                result.put("error", "No depots found");
                result.put("totalImpact", 0);
                result.put("totalTime", 0);
                result.put("budget", 0);
                return result;
            }
          
            List<Vehicle> vehicles = fetchVehicles();
            System.out.println("Vehicles received: " + vehicles.size());
            
            if (vehicles.isEmpty()) {
                result.put("error", "No vehicles found");
                result.put("totalImpact", 0);
                result.put("totalTime", 0);
                result.put("budget", 0);
                return result;
            }
         
            int budget = depots.get(0).getMechanicHours();
            int depotId = depots.get(0).getId();
            
            System.out.println("\n=== Solving Knapsack Problem ===");
            System.out.println("Budget: " + budget + " hours");
            System.out.println("Total Vehicles: " + vehicles.size());
       
            long startTime = System.currentTimeMillis();
            List<Vehicle> selected = KnapsackSolver.solve(vehicles, budget);
            long endTime = System.currentTimeMillis();
            
            int totalImpact = selected.stream().mapToInt(Vehicle::getImpact).sum();
            int totalTime = selected.stream().mapToInt(Vehicle::getDuration).sum();
            
            System.out.println("\n=== Results ===");
            System.out.println("Selected Vehicles: " + selected.size());
            System.out.println("Total Impact: " + totalImpact);
            System.out.println("Total Time Used: " + totalTime + " / " + budget + " hours");
            
            result.put("selectedVehicles", selected);
            result.put("totalImpact", totalImpact);
            result.put("totalTime", totalTime);
            result.put("budget", budget);
            result.put("depotId", depotId);
            result.put("executionTimeMs", endTime - startTime);
            result.put("message", "Success!");
            
        } catch (Exception e) {
            logger.log("backend", "error", "service", "Error: " + e.getMessage());
            e.printStackTrace();
            result.put("error", e.getMessage());
            result.put("totalImpact", 0);
            result.put("totalTime", 0);
            result.put("budget", 0);
        }
        
        return result;
    }
    
    private List<Depot> fetchDepots() {
        List<Depot> depots = new ArrayList<>();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                serverUrl + "/depots",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            System.out.println("Depots API Response: " + responseBody);
            
            if (responseBody != null && responseBody.containsKey("depots")) {
                List<Map<String, Object>> depotsList = (List<Map<String, Object>>) responseBody.get("depots");
                for (Map<String, Object> depotMap : depotsList) {
                    Depot depot = new Depot();
                   
                    if (depotMap.containsKey("ID")) {
                        depot.setId((Integer) depotMap.get("ID"));
                    }
                    if (depotMap.containsKey("MechanicHours")) {
                        depot.setMechanicHours((Integer) depotMap.get("MechanicHours"));
                    }
                    depots.add(depot);
                    System.out.println("Depot " + depot.getId() + ": " + depot.getMechanicHours() + " hours");
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching depots: " + e.getMessage());
            e.printStackTrace();
        }
        
        return depots;
    }
    
    private List<Vehicle> fetchVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                serverUrl + "/vehicles",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            System.out.println("Vehicles API Response: " + responseBody);
            
            if (responseBody != null && responseBody.containsKey("vehicles")) {
                List<Map<String, Object>> vehiclesList = (List<Map<String, Object>>) responseBody.get("vehicles");
                for (Map<String, Object> vehicleMap : vehiclesList) {
                    Vehicle vehicle = new Vehicle();
                    
                    if (vehicleMap.containsKey("TaskID")) {
                        vehicle.setTaskId((String) vehicleMap.get("TaskID"));
                    }
                    if (vehicleMap.containsKey("Duration")) {
                        vehicle.setDuration((Integer) vehicleMap.get("Duration"));
                    }
                    if (vehicleMap.containsKey("Impact")) {
                        vehicle.setImpact((Integer) vehicleMap.get("Impact"));
                    }
                    vehicles.add(vehicle);
                    System.out.println("Vehicle: " + vehicle.getTaskId().substring(0, 8) + "... | Duration: " + vehicle.getDuration() + " | Impact: " + vehicle.getImpact());
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicles;
    }
}
