package com.affordmed.vehiclescheduler.utils;

import com.affordmed.vehiclescheduler.model.Vehicle;
import java.util.*;

public class KnapsackSolver {
    
    public static List<Vehicle> solve(List<Vehicle> vehicles, int maxHours) {
        int n = vehicles.size();
        int[][] dp = new int[n + 1][maxHours + 1];
       
        for (int i = 1; i <= n; i++) {
            Vehicle v = vehicles.get(i - 1);
            for (int w = 0; w <= maxHours; w++) {
                if (v.getDuration() <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], 
                                       dp[i - 1][w - v.getDuration()] + v.getImpact());
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        List<Vehicle> selected = new ArrayList<>();
        int w = maxHours;
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Vehicle v = vehicles.get(i - 1);
                selected.add(v);
                w -= v.getDuration();
            }
        }
        
        return selected;
    }
}
