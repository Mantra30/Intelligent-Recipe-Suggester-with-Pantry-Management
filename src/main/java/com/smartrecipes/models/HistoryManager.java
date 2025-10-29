package com.smartrecipes.models;

import com.smartrecipes.utils.FileHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages cooking history and tracks recipe frequency
 */
public class HistoryManager {
    private List<CookingEntry> cookingHistory;
    private Map<String, Integer> recipeFrequency;
    private static final String HISTORY_FILE = "data/history.json";
    
    public HistoryManager() {
        this.cookingHistory = new ArrayList<>();
        this.recipeFrequency = new HashMap<>();
        loadHistory();
    }
    
    /**
     * Represents a single cooking entry
     */
    public static class CookingEntry {
        private String id;
        private String recipeId;
        private String recipeTitle;
        private LocalDateTime cookedAt;
        private int servings;
        private String notes;
        
        // Default constructor for JSON deserialization
        public CookingEntry() {}
        
        public CookingEntry(String recipeId, String recipeTitle, int servings, String notes) {
            this.id = "entry_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            this.recipeId = recipeId;
            this.recipeTitle = recipeTitle;
            this.cookedAt = LocalDateTime.now();
            this.servings = servings;
            this.notes = notes;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getRecipeId() { return recipeId; }
        public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
        
        public String getRecipeTitle() { return recipeTitle; }
        public void setRecipeTitle(String recipeTitle) { this.recipeTitle = recipeTitle; }
        
        public LocalDateTime getCookedAt() { return cookedAt; }
        public void setCookedAt(LocalDateTime cookedAt) { this.cookedAt = cookedAt; }
        
        public int getServings() { return servings; }
        public void setServings(int servings) { this.servings = servings; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        /**
         * Get formatted cooking date
         */
        public String getFormattedDate() {
            return cookedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        }
        
        /**
         * Get days since cooking
         */
        public long getDaysSinceCooking() {
            return java.time.temporal.ChronoUnit.DAYS.between(cookedAt.toLocalDate(), LocalDateTime.now().toLocalDate());
        }
    }
    
    /**
     * Load cooking history from JSON file
     */
    public void loadHistory() {
        try {
            cookingHistory = FileHandler.loadCookingHistory(HISTORY_FILE);
            updateRecipeFrequency();
        } catch (Exception e) {
            System.err.println("Error loading cooking history: " + e.getMessage());
            cookingHistory.clear();
            recipeFrequency.clear();
        }
    }
    
    /**
     * Save cooking history to JSON file
     */
    public void saveHistory() {
        try {
            FileHandler.saveCookingHistory(HISTORY_FILE, cookingHistory);
        } catch (Exception e) {
            System.err.println("Error saving cooking history: " + e.getMessage());
        }
    }
    
    /**
     * Add a new cooking entry
     */
    public void addCookingEntry(String recipeId, String recipeTitle, int servings, String notes) {
        CookingEntry entry = new CookingEntry(recipeId, recipeTitle, servings, notes);
        cookingHistory.add(entry);
        updateRecipeFrequency();
        saveHistory();
    }
    
    /**
     * Get all cooking history
     */
    public List<CookingEntry> getAllHistory() {
        return new ArrayList<>(cookingHistory);
    }
    
    /**
     * Get recent cooking history (last N entries)
     */
    public List<CookingEntry> getRecentHistory(int count) {
        return cookingHistory.stream()
                .sorted((e1, e2) -> e2.getCookedAt().compareTo(e1.getCookedAt()))
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * Get cooking history for a specific recipe
     */
    public List<CookingEntry> getHistoryForRecipe(String recipeId) {
        return cookingHistory.stream()
                .filter(entry -> recipeId.equals(entry.getRecipeId()))
                .sorted((e1, e2) -> e2.getCookedAt().compareTo(e1.getCookedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get most frequently cooked recipes
     */
    public List<Map.Entry<String, Integer>> getMostCookedRecipes(int count) {
        return recipeFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * Get cooking frequency for a recipe
     */
    public int getCookingFrequency(String recipeId) {
        return recipeFrequency.getOrDefault(recipeId, 0);
    }
    
    /**
     * Get cooking statistics
     */
    public Map<String, Object> getCookingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCookingSessions", cookingHistory.size());
        stats.put("uniqueRecipesCooked", recipeFrequency.size());
        
        if (!cookingHistory.isEmpty()) {
            // Most recent cooking
            CookingEntry mostRecent = cookingHistory.stream()
                    .max(Comparator.comparing(CookingEntry::getCookedAt))
                    .orElse(null);
            stats.put("mostRecentCooking", mostRecent != null ? mostRecent.getFormattedDate() : "Never");
            
            // Average servings
            double avgServings = cookingHistory.stream()
                    .mapToInt(CookingEntry::getServings)
                    .average()
                    .orElse(0.0);
            stats.put("averageServings", Math.round(avgServings * 10.0) / 10.0);
        }
        
        // Cooking frequency distribution
        Map<String, Long> frequencyDistribution = recipeFrequency.values().stream()
                .collect(Collectors.groupingBy(freq -> {
                    if (freq == 1) return "Once";
                    else if (freq <= 3) return "2-3 times";
                    else if (freq <= 5) return "4-5 times";
                    else return "5+ times";
                }, Collectors.counting()));
        
        stats.put("frequencyDistribution", frequencyDistribution);
        
        return stats;
    }
    
    /**
     * Get cooking trends by month
     */
    public Map<String, Integer> getCookingTrendsByMonth() {
        return cookingHistory.stream()
                .collect(Collectors.groupingBy(
                    entry -> entry.getCookedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }
    
    /**
     * Delete a cooking entry
     */
    public boolean deleteCookingEntry(String entryId) {
        boolean removed = cookingHistory.removeIf(entry -> entryId.equals(entry.getId()));
        if (removed) {
            updateRecipeFrequency();
            saveHistory();
        }
        return removed;
    }
    
    /**
     * Clear all cooking history
     */
    public void clearHistory() {
        cookingHistory.clear();
        recipeFrequency.clear();
        saveHistory();
    }
    
    /**
     * Update recipe frequency map
     */
    private void updateRecipeFrequency() {
        recipeFrequency.clear();
        for (CookingEntry entry : cookingHistory) {
            recipeFrequency.merge(entry.getRecipeId(), 1, Integer::sum);
        }
    }
    
    /**
     * Get cooking streaks (consecutive days of cooking)
     */
    public int getCurrentCookingStreak() {
        if (cookingHistory.isEmpty()) return 0;
        
        List<LocalDateTime> sortedDates = cookingHistory.stream()
                .map(CookingEntry::getCookedAt)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        
        int streak = 0;
        LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        for (LocalDateTime cookingDate : sortedDates) {
            LocalDateTime cookingDay = cookingDate.toLocalDate().atStartOfDay();
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(cookingDay, currentDate);
            
            if (daysDiff == streak) {
                streak++;
                currentDate = cookingDay;
            } else if (daysDiff > streak) {
                break;
            }
        }
        
        return streak;
    }
}
