package com.smartrecipes.models;

import com.smartrecipes.utils.FileHandler;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages pantry inventory with CRUD operations and expiry tracking
 */
public class PantryManager {
    private Map<String, Ingredient> ingredients;
    private static final String PANTRY_FILE = "data/pantry.json";
    
    public PantryManager() {
        this.ingredients = new HashMap<>();
        loadPantry();
    }
    
    /**
     * Load pantry data from JSON file
     */
    public void loadPantry() {
        try {
            List<Ingredient> ingredientList = FileHandler.loadIngredients(PANTRY_FILE);
            ingredients.clear();
            for (Ingredient ingredient : ingredientList) {
                ingredients.put(ingredient.getName().toLowerCase(), ingredient);
            }
        } catch (Exception e) {
            System.err.println("Error loading pantry: " + e.getMessage());
            ingredients.clear();
        }
    }
    
    /**
     * Save pantry data to JSON file
     */
    public void savePantry() {
        try {
            List<Ingredient> ingredientList = new ArrayList<>(ingredients.values());
            FileHandler.saveIngredients(PANTRY_FILE, ingredientList);
        } catch (Exception e) {
            System.err.println("Error saving pantry: " + e.getMessage());
        }
    }
    
    /**
     * Add or update an ingredient in the pantry
     */
    public void addIngredient(Ingredient ingredient) {
        String key = ingredient.getName().toLowerCase();
        ingredients.put(key, ingredient);
        savePantry();
    }
    
    /**
     * Remove an ingredient from the pantry
     */
    public boolean removeIngredient(String ingredientName) {
        String key = ingredientName.toLowerCase();
        Ingredient removed = ingredients.remove(key);
        if (removed != null) {
            savePantry();
            return true;
        }
        return false;
    }
    
    /**
     * Get an ingredient by name
     */
    public Ingredient getIngredient(String ingredientName) {
        return ingredients.get(ingredientName.toLowerCase());
    }
    
    /**
     * Get all ingredients in the pantry
     */
    public List<Ingredient> getAllIngredients() {
        return new ArrayList<>(ingredients.values());
    }
    
    /**
     * Get ingredients by category
     */
    public List<Ingredient> getIngredientsByCategory(String category) {
        return ingredients.values().stream()
                .filter(ingredient -> category.equalsIgnoreCase(ingredient.getCategory()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get expiring ingredients within specified days
     */
    public List<Ingredient> getExpiringIngredients(int days) {
        return ingredients.values().stream()
                .filter(ingredient -> ingredient.isExpiringSoon(days))
                .sorted(Comparator.comparing(Ingredient::getExpiryDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Get expired ingredients
     */
    public List<Ingredient> getExpiredIngredients() {
        return ingredients.values().stream()
                .filter(Ingredient::isExpired)
                .sorted(Comparator.comparing(Ingredient::getExpiryDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Get low stock ingredients (quantity < threshold)
     */
    public List<Ingredient> getLowStockIngredients(double threshold) {
        return ingredients.values().stream()
                .filter(ingredient -> ingredient.getQuantity() < threshold)
                .collect(Collectors.toList());
    }
    
    /**
     * Update ingredient quantity
     */
    public boolean updateQuantity(String ingredientName, double newQuantity) {
        Ingredient ingredient = getIngredient(ingredientName);
        if (ingredient != null) {
            ingredient.setQuantity(newQuantity);
            savePantry();
            return true;
        }
        return false;
    }
    
    /**
     * Consume ingredients for a recipe
     */
    public boolean consumeIngredients(List<String> recipeIngredients) {
        boolean allConsumed = true;
        
        for (String recipeIngredient : recipeIngredients) {
            // Try to find matching ingredient in pantry
            Ingredient pantryIngredient = findMatchingIngredient(recipeIngredient);
            if (pantryIngredient != null) {
                // Reduce quantity (assuming 1 unit per recipe)
                double newQuantity = pantryIngredient.getQuantity() - 1.0;
                if (newQuantity <= 0) {
                    removeIngredient(pantryIngredient.getName());
                } else {
                    pantryIngredient.setQuantity(newQuantity);
                }
            } else {
                allConsumed = false;
            }
        }
        
        if (allConsumed) {
            savePantry();
        }
        return allConsumed;
    }
    
    /**
     * Find matching ingredient using fuzzy matching
     */
    private Ingredient findMatchingIngredient(String recipeIngredient) {
        // Simple matching - can be enhanced with fuzzy matching
        String normalizedRecipe = recipeIngredient.toLowerCase().trim();
        
        // Direct match
        Ingredient directMatch = ingredients.get(normalizedRecipe);
        if (directMatch != null) {
            return directMatch;
        }
        
        // Partial match
        for (Ingredient ingredient : ingredients.values()) {
            if (ingredient.getName().toLowerCase().contains(normalizedRecipe) ||
                normalizedRecipe.contains(ingredient.getName().toLowerCase())) {
                return ingredient;
            }
        }
        
        return null;
    }
    
    /**
     * Search ingredients by name
     */
    public List<Ingredient> searchIngredients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllIngredients();
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        return ingredients.values().stream()
                .filter(ingredient -> ingredient.getName().toLowerCase().contains(normalizedQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all categories in the pantry
     */
    public Set<String> getAllCategories() {
        return ingredients.values().stream()
                .map(Ingredient::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get pantry statistics
     */
    public Map<String, Object> getPantryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIngredients", ingredients.size());
        stats.put("expiringSoon", getExpiringIngredients(7).size());
        stats.put("expired", getExpiredIngredients().size());
        stats.put("lowStock", getLowStockIngredients(2.0).size());
        stats.put("categories", getAllCategories().size());
        return stats;
    }
}
