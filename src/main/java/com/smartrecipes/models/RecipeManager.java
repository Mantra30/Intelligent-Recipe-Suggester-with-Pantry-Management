package com.smartrecipes.models;

import com.smartrecipes.utils.FileHandler;
import com.smartrecipes.utils.FuzzyMatcher;
import com.smartrecipes.utils.RecipeScraper;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages recipe database with search, filtering, and suggestion capabilities
 */
public class RecipeManager {
    private List<Recipe> recipes;
    private Map<String, Recipe> recipeMap;
    private static final String RECIPES_FILE = "data/recipes.json";
    
    public RecipeManager() {
        this.recipes = new ArrayList<>();
        this.recipeMap = new HashMap<>();
        loadRecipes();
    }
    
    /**
     * Load recipes from JSON file
     */
    public void loadRecipes() {
        try {
            recipes = FileHandler.loadRecipes(RECIPES_FILE);
            recipeMap.clear();
            for (Recipe recipe : recipes) {
                recipeMap.put(recipe.getId(), recipe);
            }
        } catch (Exception e) {
            System.err.println("Error loading recipes: " + e.getMessage());
            recipes.clear();
        }
    }
    
    /**
     * Save recipes to JSON file
     */
    public void saveRecipes() {
        try {
            FileHandler.saveRecipes(RECIPES_FILE, recipes);
        } catch (Exception e) {
            System.err.println("Error saving recipes: " + e.getMessage());
        }
    }
    
    /**
     * Get all recipes
     */
    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }
    
    /**
     * Get recipe by ID
     */
    public Recipe getRecipeById(String id) {
        return recipeMap.get(id);
    }
    
    /**
     * Add a new recipe
     */
    public void addRecipe(Recipe recipe) {
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(generateRecipeId());
        }
        recipes.add(recipe);
        recipeMap.put(recipe.getId(), recipe);
        saveRecipes();
    }
    
    /**
     * Update an existing recipe
     */
    public boolean updateRecipe(Recipe recipe) {
        Recipe existing = recipeMap.get(recipe.getId());
        if (existing != null) {
            int index = recipes.indexOf(existing);
            recipes.set(index, recipe);
            recipeMap.put(recipe.getId(), recipe);
            saveRecipes();
            return true;
        }
        return false;
    }
    
    /**
     * Delete a recipe
     */
    public boolean deleteRecipe(String recipeId) {
        Recipe recipe = recipeMap.remove(recipeId);
        if (recipe != null) {
            recipes.remove(recipe);
            saveRecipes();
            return true;
        }
        return false;
    }
    
    /**
     * Search recipes by title or description
     */
    public List<Recipe> searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllRecipes();
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        return recipes.stream()
                .filter(recipe -> recipe.getTitle().toLowerCase().contains(normalizedQuery) ||
                                (recipe.getDescription() != null && 
                                 recipe.getDescription().toLowerCase().contains(normalizedQuery)))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter recipes by cuisine
     */
    public List<Recipe> getRecipesByCuisine(String cuisine) {
        return recipes.stream()
                .filter(recipe -> cuisine.equalsIgnoreCase(recipe.getCuisine()))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter recipes by difficulty
     */
    public List<Recipe> getRecipesByDifficulty(String difficulty) {
        return recipes.stream()
                .filter(recipe -> difficulty.equalsIgnoreCase(recipe.getDifficulty()))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter recipes by cooking time
     */
    public List<Recipe> getRecipesByMaxTime(int maxTime) {
        return recipes.stream()
                .filter(recipe -> recipe.getTotalTime() <= maxTime)
                .collect(Collectors.toList());
    }
    
    /**
     * Get recipes that can be made with available ingredients
     */
    public List<Recipe> getSuggestedRecipes(List<String> availableIngredients, PantryManager pantryManager) {
        List<Recipe> suggestions = new ArrayList<>();
        
        for (Recipe recipe : recipes) {
            double matchScore = calculateIngredientMatch(recipe.getIngredients(), availableIngredients);
            if (matchScore > 0.3) { // At least 30% ingredient match
                suggestions.add(recipe);
            }
        }
        
        // Sort by match score (descending)
        suggestions.sort((r1, r2) -> {
            double score1 = calculateIngredientMatch(r1.getIngredients(), availableIngredients);
            double score2 = calculateIngredientMatch(r2.getIngredients(), availableIngredients);
            return Double.compare(score2, score1);
        });
        
        return suggestions;
    }
    
    /**
     * Calculate ingredient match score between recipe and available ingredients
     */
    private double calculateIngredientMatch(List<String> recipeIngredients, List<String> availableIngredients) {
        if (recipeIngredients.isEmpty()) return 0.0;
        
        int matches = 0;
        for (String recipeIngredient : recipeIngredients) {
            if (hasMatchingIngredient(recipeIngredient, availableIngredients)) {
                matches++;
            }
        }
        
        return (double) matches / recipeIngredients.size();
    }
    
    /**
     * Check if recipe ingredient matches any available ingredient
     */
    private boolean hasMatchingIngredient(String recipeIngredient, List<String> availableIngredients) {
        String normalizedRecipe = recipeIngredient.toLowerCase().trim();
        
        for (String available : availableIngredients) {
            String normalizedAvailable = available.toLowerCase().trim();
            
            // Direct match
            if (normalizedRecipe.equals(normalizedAvailable)) {
                return true;
            }
            
            // Partial match
            if (normalizedRecipe.contains(normalizedAvailable) || 
                normalizedAvailable.contains(normalizedRecipe)) {
                return true;
            }
            
            // Fuzzy match using FuzzyMatcher
            if (FuzzyMatcher.calculateSimilarity(normalizedRecipe, normalizedAvailable) > 0.7) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all unique cuisines
     */
    public Set<String> getAllCuisines() {
        return recipes.stream()
                .map(Recipe::getCuisine)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get all unique difficulties
     */
    public Set<String> getAllDifficulties() {
        return recipes.stream()
                .map(Recipe::getDifficulty)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get custom recipes only
     */
    public List<Recipe> getCustomRecipes() {
        return recipes.stream()
                .filter(Recipe::isCustom)
                .collect(Collectors.toList());
    }
    
    /**
     * Get recipes with specific tags
     */
    public List<Recipe> getRecipesByTag(String tag) {
        return recipes.stream()
                .filter(recipe -> recipe.getTags().contains(tag))
                .collect(Collectors.toList());
    }
    
    /**
     * Generate unique recipe ID
     */
    private String generateRecipeId() {
        return "recipe_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Get recipe statistics
     */
    public Map<String, Object> getRecipeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecipes", recipes.size());
        stats.put("customRecipes", getCustomRecipes().size());
        stats.put("cuisines", getAllCuisines().size());
        stats.put("difficulties", getAllDifficulties().size());
        
        // Average cooking time
        double avgTime = recipes.stream()
                .mapToInt(Recipe::getTotalTime)
                .average()
                .orElse(0.0);
        stats.put("averageCookingTime", Math.round(avgTime));
        
        return stats;
    }
    
    /**
     * Scrape recipes from web based on search query
     */
    public List<Recipe> scrapeRecipesFromWeb(String searchQuery) {
        List<Recipe> scrapedRecipes = RecipeScraper.scrapeRecipes(searchQuery);
        
        // Add scraped recipes to the database
        for (Recipe recipe : scrapedRecipes) {
            if (!recipeExists(recipe.getTitle())) {
                addRecipe(recipe);
            }
        }
        
        return scrapedRecipes;
    }
    
    /**
     * Search recipes by available ingredients using web scraping
     */
    public List<Recipe> searchRecipesByIngredients(List<String> ingredients) {
        List<Recipe> webRecipes = RecipeScraper.searchRecipesByIngredients(ingredients);
        
        // Combine with existing recipes
        List<Recipe> allRecipes = new ArrayList<>(recipes);
        allRecipes.addAll(webRecipes);
        
        // Filter and rank by ingredient match
        return getSuggestedRecipes(ingredients, null);
    }
    
    /**
     * Check if recipe already exists
     */
    private boolean recipeExists(String title) {
        return recipes.stream().anyMatch(recipe -> recipe.getTitle().equalsIgnoreCase(title));
    }
    
    /**
     * Get best recipe recommendations with scoring
     */
    public List<Recipe> getBestRecipeRecommendations(List<String> availableIngredients, int maxResults) {
        List<Recipe> suggestions = getSuggestedRecipes(availableIngredients, null);
        
        // Sort by match score and other factors
        suggestions.sort((r1, r2) -> {
            double score1 = calculateRecipeScore(r1, availableIngredients);
            double score2 = calculateRecipeScore(r2, availableIngredients);
            return Double.compare(score2, score1);
        });
        
        return suggestions.stream().limit(maxResults).collect(Collectors.toList());
    }
    
    /**
     * Calculate comprehensive recipe score
     */
    private double calculateRecipeScore(Recipe recipe, List<String> availableIngredients) {
        double ingredientMatch = calculateIngredientMatch(recipe.getIngredients(), availableIngredients);
        double difficultyScore = getDifficultyScore(recipe.getDifficulty());
        double timeScore = getTimeScore(recipe.getTotalTime());
        
        // Weighted scoring: 70% ingredient match, 20% difficulty, 10% time
        return (ingredientMatch * 0.7) + (difficultyScore * 0.2) + (timeScore * 0.1);
    }
    
    /**
     * Get difficulty score (lower difficulty = higher score)
     */
    private double getDifficultyScore(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return 1.0;
            case "medium": return 0.7;
            case "hard": return 0.4;
            default: return 0.5;
        }
    }
    
    /**
     * Get time score (shorter time = higher score)
     */
    private double getTimeScore(int totalTime) {
        if (totalTime <= 15) return 1.0;
        if (totalTime <= 30) return 0.8;
        if (totalTime <= 60) return 0.6;
        return 0.4;
    }
}
