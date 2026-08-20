package com.smartrecipes.utils;

import com.smartrecipes.models.Ingredient;
import com.smartrecipes.models.Recipe;
import com.smartrecipes.models.HistoryManager.CookingEntry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple file handler without Jackson dependency
 */
public class FileHandler {
    
    /**
     * Load ingredients from JSON file (simplified)
     * Returns empty list if file doesn't exist (don't create sample data)
     */
    public static List<Ingredient> loadIngredients(String filePath) throws IOException {
        List<Ingredient> ingredients = new ArrayList<>();

        // Return empty list if file doesn't exist (don't create sample data)
        if (!Files.exists(Paths.get(filePath))) {
            return ingredients;
        }

        // Tolerant parsing: read objects between { ... } and extract fields in any order
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean inObject = false;
            String name = null;
            String unit = null;
            String category = null;
            Double quantity = null;
            LocalDate expiry = null;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("{")) {
                    inObject = true;
                    name = null; unit = null; category = null; quantity = null; expiry = null;
                    continue;
                }
                if (trimmed.startsWith("}")) {
                    if (inObject && name != null && quantity != null && unit != null && expiry != null) {
                        ingredients.add(new Ingredient(name, quantity, unit, expiry, category));
                    }
                    inObject = false;
                    continue;
                }
                if (!inObject) continue;

                if (trimmed.startsWith("\"name\"")) {
                    name = extractValue(trimmed);
                } else if (trimmed.startsWith("\"quantity\"")) {
                    String val = extractValueOrNumber(trimmed);
                    try { quantity = Double.parseDouble(val); } catch (NumberFormatException ignored) {}
                } else if (trimmed.startsWith("\"unit\"")) {
                    unit = extractValue(trimmed);
                } else if (trimmed.startsWith("\"expiry\"") || trimmed.startsWith("\"expiryDate\"")) {
                    String v = extractValue(trimmed);
                    try { expiry = LocalDate.parse(v); } catch (Exception ignored) {}
                } else if (trimmed.startsWith("\"category\"")) {
                    category = extractValue(trimmed);
                }
            }
        }

        return ingredients;
    }
    
    /**
     * Save ingredients to JSON file (simplified)
     */
    public static void saveIngredients(String filePath, List<Ingredient> ingredients) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("[");
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                writer.println("  {");
                writer.println("    \"name\": \"" + ingredient.getName() + "\",");
                writer.println("    \"quantity\": " + ingredient.getQuantity() + ",");
                writer.println("    \"unit\": \"" + ingredient.getUnit() + "\",");
                writer.println("    \"expiryDate\": \"" + ingredient.getExpiryDate() + "\",");
                writer.println("    \"category\": \"" + ingredient.getCategory() + "\"");
                writer.print("  }");
                if (i < ingredients.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("]");
        }
    }
    
    /**
     * Load recipes from JSON file (simplified)
     */
    public static List<Recipe> loadRecipes(String filePath) throws IOException {
        List<Recipe> recipes = new ArrayList<>();
        
        // Create sample recipes if file doesn't exist
        if (!Files.exists(Paths.get(filePath))) {
            Recipe recipe1 = new Recipe("recipe_001", "Paneer Butter Masala", 
                "A rich and creamy North Indian curry", 
                List.of("200g paneer", "2 large tomatoes", "1 large onion", "2 cloves garlic"),
                List.of("Cut paneer into cubes", "Blend tomatoes and onions", "Cook with spices"),
                "Indian", "Medium", 15, 25, 4);
            recipes.add(recipe1);
            
            Recipe recipe2 = new Recipe("recipe_002", "Chicken Fried Rice",
                "Chinese-style fried rice with chicken",
                List.of("2 cups rice", "200g chicken", "1 cup vegetables", "2 eggs"),
                List.of("Cook rice", "Stir-fry chicken", "Add vegetables", "Mix everything"),
                "Chinese", "Easy", 10, 15, 4);
            recipes.add(recipe2);
            
            return recipes;
        }
        
        // Simplified JSON parsing for recipes
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("\"title\"")) {
                    String title = extractValue(line);
                    String description = extractValue(reader.readLine());
                    List<String> ingredients = new ArrayList<>();
                    List<String> steps = new ArrayList<>();
                    String cuisine = "Indian";
                    String difficulty = "Easy";
                    int prepTime = 15;
                    int cookTime = 30;
                    int servings = 4;
                    
                    recipes.add(new Recipe("recipe_" + recipes.size(), title, description,
                        ingredients, steps, cuisine, difficulty, prepTime, cookTime, servings));
                }
            }
        }
        
        return recipes;
    }
    
    /**
     * Save recipes to JSON file (simplified)
     */
    public static void saveRecipes(String filePath, List<Recipe> recipes) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("[");
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                writer.println("  {");
                writer.println("    \"id\": \"" + recipe.getId() + "\",");
                writer.println("    \"title\": \"" + recipe.getTitle() + "\",");
                writer.println("    \"description\": \"" + recipe.getDescription() + "\",");
                writer.println("    \"cuisine\": \"" + recipe.getCuisine() + "\",");
                writer.println("    \"difficulty\": \"" + recipe.getDifficulty() + "\",");
                writer.println("    \"prepTime\": " + recipe.getPrepTime() + ",");
                writer.println("    \"cookTime\": " + recipe.getCookTime() + ",");
                writer.println("    \"servings\": " + recipe.getServings());
                writer.print("  }");
                if (i < recipes.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("]");
        }
    }
    
    /**
     * Load cooking history from JSON file (simplified)
     */
    public static List<CookingEntry> loadCookingHistory(String filePath) throws IOException {
        List<CookingEntry> history = new ArrayList<>();
        
        if (!Files.exists(Paths.get(filePath))) {
            return history; // Return empty list
        }
        
        // Simplified parsing - return empty list for now
        return history;
    }
    
    /**
     * Save cooking history to JSON file (simplified)
     */
    public static void saveCookingHistory(String filePath, List<CookingEntry> history) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("[]");
        }
    }
    
    /**
     * Extract value from JSON line
     */
    private static String extractValue(String line) {
        if (line == null) return "";
        int start = line.indexOf("\"");
        if (start == -1) return "";
        start++;
        int end = line.lastIndexOf("\"");
        if (end == -1 || end <= start) return "";
        return line.substring(start, end);
    }

    // Extracts number value as string from a JSON line like: "quantity": 1,
    private static String extractValueOrNumber(String line) {
        if (line == null) return "";
        int colon = line.indexOf(":");
        if (colon == -1) return "";
        String after = line.substring(colon + 1).trim();
        // strip trailing comma
        if (after.endsWith(",")) after = after.substring(0, after.length() - 1).trim();
        // if quoted, reuse extractValue
        if (after.startsWith("\"")) return extractValue(after);
        return after;
    }
    
    /**
     * Check if a file exists
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Create directory if it doesn't exist
     */
    public static void createDirectoryIfNotExists(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }
}