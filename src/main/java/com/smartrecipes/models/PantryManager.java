package com.smartrecipes.models;

import com.smartrecipes.utils.FileHandler;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages pantry inventory with CRUD operations and expiry tracking
 */
public class PantryManager {
    private Map<String, Ingredient> ingredients;
    private static final String PANTRY_FILE = resolvePantryPath();
    private static final String LEGACY_PANTRY_FILE = "data/pantry.json";
    
    public PantryManager() {
        this.ingredients = new HashMap<>();
        migratePantryIfNeeded();
        loadPantry();
    }

    private static String resolvePantryPath() {
        String base = System.getProperty("user.home");
        String dir = base + File.separator + ".smartrecipes";
        try {
            com.smartrecipes.utils.FileHandler.createDirectoryIfNotExists(dir);
        } catch (Exception ignored) {}
        return dir + File.separator + "pantry.json";
    }

    private void migratePantryIfNeeded() {
        try {
            boolean newExists = com.smartrecipes.utils.FileHandler.fileExists(PANTRY_FILE);
            if (newExists) {
                return; // New file exists, no migration needed
            }
            
            // Try multiple legacy locations
            String[] legacyPaths = {
                LEGACY_PANTRY_FILE,
                "src/main/resources/data/pantry.json",
                System.getProperty("user.dir") + File.separator + "data" + File.separator + "pantry.json",
                System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator + "pantry.json"
            };
            
            for (String legacyPath : legacyPaths) {
                if (com.smartrecipes.utils.FileHandler.fileExists(legacyPath)) {
                    try {
                        List<Ingredient> legacy = com.smartrecipes.utils.FileHandler.loadIngredients(legacyPath);
                ingredients.clear();
                for (Ingredient i : legacy) {
                    ingredients.put(i.getName().toLowerCase(), i);
                }
                savePantry();
                        System.out.println("Migrated pantry from: " + legacyPath);
                        return;
                    } catch (Exception e) {
                        System.err.println("Failed to migrate from " + legacyPath + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during pantry migration: " + e.getMessage());
        }
    }
    
    /**
     * Load pantry data from JSON file
     */
    public void loadPantry() {
        System.out.println("Attempting to load pantry from: " + PANTRY_FILE);
        System.out.println("File exists: " + com.smartrecipes.utils.FileHandler.fileExists(PANTRY_FILE));
        
        // First try the primary location
        if (com.smartrecipes.utils.FileHandler.fileExists(PANTRY_FILE)) {
        try {
            List<Ingredient> ingredientList = FileHandler.loadIngredients(PANTRY_FILE);
                if (ingredientList != null && !ingredientList.isEmpty()) {
            ingredients.clear();
            for (Ingredient ingredient : ingredientList) {
                        if (ingredient != null && ingredient.getName() != null) {
                ingredients.put(ingredient.getName().toLowerCase(), ingredient);
                        }
                    }
                    System.out.println("✓ Loaded " + ingredients.size() + " pantry items from " + PANTRY_FILE);
                    return;
                } else {
                    System.out.println("⚠ Pantry file exists but is empty: " + PANTRY_FILE);
                }
            } catch (Exception e) {
                System.err.println("✗ Error loading pantry from " + PANTRY_FILE + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Pantry file does not exist: " + PANTRY_FILE);
        }
        
        // Try legacy locations
        String[] legacyPaths = {
            "src/main/resources/data/pantry.json",
            System.getProperty("user.dir") + File.separator + "data" + File.separator + "pantry.json",
            System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator + "pantry.json",
            "data/pantry.json"
        };
        
        for (String legacyPath : legacyPaths) {
            if (com.smartrecipes.utils.FileHandler.fileExists(legacyPath)) {
                try {
                    System.out.println("Trying legacy location: " + legacyPath);
                    List<Ingredient> legacyList = FileHandler.loadIngredients(legacyPath);
                    if (legacyList != null && !legacyList.isEmpty()) {
                        ingredients.clear();
                        for (Ingredient ingredient : legacyList) {
                            ingredients.put(ingredient.getName().toLowerCase(), ingredient);
                        }
                        System.out.println("✓ Loaded " + ingredients.size() + " pantry items from legacy location: " + legacyPath);
                        savePantry(); // Save to new location
                        return;
            }
        } catch (Exception e) {
                    System.err.println("✗ Failed to load from " + legacyPath + ": " + e.getMessage());
                }
            }
        }
        
        // If no file found, check if we have items in memory (from migration)
        if (ingredients.isEmpty()) {
            System.out.println("⚠ No pantry file found. Starting with empty pantry.");
        } else {
            System.out.println("ℹ Using " + ingredients.size() + " items from in-memory pantry.");
        }
    }
    
    /**
     * Reload pantry from file (useful for refresh)
     */
    public void reloadPantry() {
        loadPantry();
    }
    
    /**
     * Save pantry data to JSON file
     */
    public void savePantry() {
        try {
            List<Ingredient> ingredientList = new ArrayList<>(ingredients.values());
            FileHandler.saveIngredients(PANTRY_FILE, ingredientList);
            System.out.println("✓ Saved " + ingredientList.size() + " pantry items to " + PANTRY_FILE);
        } catch (Exception e) {
            System.err.println("✗ Error saving pantry to " + PANTRY_FILE + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add or update an ingredient in the pantry (with duplicate prevention)
     */
    public void addIngredient(Ingredient ingredient) {
        String key = ingredient.getName().toLowerCase().trim();
        
        // Check for similar ingredients (case-insensitive, normalized)
        Ingredient existing = ingredients.get(key);
        if (existing != null) {
            // Merge quantities if same ingredient
            double newQuantity = existing.getQuantity() + ingredient.getQuantity();
            existing.setQuantity(newQuantity);
            // Update expiry to the later date
            if (ingredient.getExpiryDate() != null && 
                (existing.getExpiryDate() == null || ingredient.getExpiryDate().isAfter(existing.getExpiryDate()))) {
                existing.setExpiryDate(ingredient.getExpiryDate());
            }
            savePantry();
            return;
        }
        
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
                    // Do not auto-remove; set to zero and keep until manual removal or expiry
                    pantryIngredient.setQuantity(0);
                } else {
                    pantryIngredient.setQuantity(newQuantity);
                }
            } else {
                allConsumed = false;
            }
        }
        
        // Always persist changes so state is kept across restarts
        savePantry();
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
