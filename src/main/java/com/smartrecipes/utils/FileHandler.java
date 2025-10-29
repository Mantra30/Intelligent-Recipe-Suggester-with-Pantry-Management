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
     */
    public static List<Ingredient> loadIngredients(String filePath) throws IOException {
        List<Ingredient> ingredients = new ArrayList<>();
        
        // Create sample ingredients if file doesn't exist
        if (!Files.exists(Paths.get(filePath))) {
            ingredients.add(new Ingredient("Rice", 2.0, "kg", LocalDate.now().plusDays(30), "Grains"));
            ingredients.add(new Ingredient("Onions", 1.0, "kg", LocalDate.now().plusDays(7), "Vegetables"));
            ingredients.add(new Ingredient("Tomatoes", 0.5, "kg", LocalDate.now().plusDays(5), "Vegetables"));
            ingredients.add(new Ingredient("Paneer", 200.0, "g", LocalDate.now().plusDays(3), "Dairy"));
            ingredients.add(new Ingredient("Chicken", 500.0, "g", LocalDate.now().plusDays(2), "Meat"));
            return ingredients;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("\"name\"")) {
                    String name = extractValue(line);
                    String quantityStr = reader.readLine();
                    double quantity = Double.parseDouble(extractValue(quantityStr));
                    String unit = extractValue(reader.readLine());
                    String expiryStr = extractValue(reader.readLine());
                    LocalDate expiry = LocalDate.parse(expiryStr);
                    String category = extractValue(reader.readLine());
                    
                    ingredients.add(new Ingredient(name, quantity, unit, expiry, category));
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