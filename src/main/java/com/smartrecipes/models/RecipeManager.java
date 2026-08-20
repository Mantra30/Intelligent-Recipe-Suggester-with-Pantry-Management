package com.smartrecipes.models;

import com.smartrecipes.utils.FileHandler;
import com.smartrecipes.utils.IngredientMatcher;
import com.smartrecipes.utils.RecipeScraper;
import com.smartrecipes.utils.RecipeSeeder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages recipe database with search, filtering, and suggestion capabilities
 */
public class RecipeManager {
    private List<Recipe> recipes;
    private Map<String, Recipe> recipeMap;
    private Map<String, List<Recipe>> searchIndex; // Fast search index
    private static final String RECIPES_FILE = "data/recipes.json";
    
    public RecipeManager() {
        this.recipes = new ArrayList<>();
        this.recipeMap = new HashMap<>();
        this.searchIndex = new HashMap<>();
        loadRecipes();
        buildSearchIndex();
    }
    
    /**
     * Build fast search index for instant recipe lookup
     */
    private void buildSearchIndex() {
        searchIndex.clear();
        for (Recipe recipe : recipes) {
            // Index by title words
            String[] titleWords = recipe.getTitle().toLowerCase().split("\\s+");
            for (String word : titleWords) {
                if (word.length() > 2) { // Ignore short words
                    searchIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(recipe);
                }
            }
            
            // Index by cuisine
            if (recipe.getCuisine() != null) {
                String cuisine = recipe.getCuisine().toLowerCase();
                searchIndex.computeIfAbsent(cuisine, k -> new ArrayList<>()).add(recipe);
            }
            
            // Index by ingredient words
            for (String ingredient : recipe.getIngredients()) {
                String[] words = ingredient.toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.length() > 3) { // Ignore very short words
                        searchIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(recipe);
                    }
                }
            }
        }
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
            // Only seed if we have very few recipes (performance optimization)
            if (recipes.size() < 50) {
                int added = RecipeSeeder.ensureMinimumRecipes(this, 50, 50);
            if (added > 0) {
                // refresh map
                recipeMap.clear();
                for (Recipe recipe : recipes) {
                    recipeMap.put(recipe.getId(), recipe);
                }
                saveRecipes();
                buildSearchIndex(); // Rebuild index after seeding
                }
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
     * Add a new recipe (with duplicate prevention)
     */
    public void addRecipe(Recipe recipe) {
        // Check for duplicates by title
        String normalizedTitle = recipe.getTitle().toLowerCase().trim();
        for (Recipe existing : recipes) {
            if (existing.getTitle().toLowerCase().trim().equals(normalizedTitle)) {
                // Update existing recipe instead of adding duplicate
                recipe.setId(existing.getId());
                updateRecipe(recipe);
                return;
            }
        }
        
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(generateRecipeId());
        }
        recipes.add(recipe);
        recipeMap.put(recipe.getId(), recipe);
        saveRecipes();
        buildSearchIndex(); // Rebuild index
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
            buildSearchIndex(); // Rebuild index
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
            buildSearchIndex(); // Rebuild index
            return true;
        }
        return false;
    }
    
    /**
     * Fast search recipes using indexed lookup
     */
    public List<Recipe> searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllRecipes();
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        String[] queryWords = normalizedQuery.split("\\s+");
        
        // Use index for fast lookup
        Map<Recipe, Integer> recipeScores = new HashMap<>();
        for (String word : queryWords) {
            if (word.length() > 2) {
                List<Recipe> matches = searchIndex.getOrDefault(word, new ArrayList<>());
                for (Recipe recipe : matches) {
                    recipeScores.put(recipe, recipeScores.getOrDefault(recipe, 0) + 1);
                }
            }
        }
        
        // Also do traditional search for partial matches
        List<Recipe> results = new ArrayList<>();
        Set<Recipe> seen = new HashSet<>();
        
        // Add indexed results
        for (Map.Entry<Recipe, Integer> entry : recipeScores.entrySet()) {
            if (entry.getValue() > 0) {
                results.add(entry.getKey());
                seen.add(entry.getKey());
            }
        }
        
        // Add partial matches from traditional search
        for (Recipe recipe : recipes) {
            if (!seen.contains(recipe)) {
                if (recipe.getTitle().toLowerCase().contains(normalizedQuery) ||
                    (recipe.getDescription() != null && 
                     recipe.getDescription().toLowerCase().contains(normalizedQuery))) {
                    results.add(recipe);
                }
            }
        }
        
        // Sort by relevance (score from index)
        results.sort((r1, r2) -> {
            int score1 = recipeScores.getOrDefault(r1, 0);
            int score2 = recipeScores.getOrDefault(r2, 0);
            return Integer.compare(score2, score1);
        });
        
        return results;
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
     * Returns maximum recipes sorted by match percentage (highest to lowest)
     */
    public List<Recipe> getSuggestedRecipes(List<String> availableIngredients, PantryManager pantryManager) {
        List<Recipe> suggestions = new ArrayList<>(recipes);
        
        // Cache match scores to avoid recalculating during sort (performance optimization)
        Map<Recipe, Double> scoreCache = new HashMap<>();
        for (Recipe recipe : suggestions) {
            scoreCache.put(recipe, calculateIngredientMatch(recipe.getIngredients(), availableIngredients));
        }
        
        // Sort by match percentage (descending) - highest match percentage at top, lowest at bottom
        suggestions.sort((r1, r2) -> {
            double score1 = scoreCache.get(r1);
            double score2 = scoreCache.get(r2);
            return Double.compare(score2, score1); // Descending: higher score first
        });
        
        return suggestions;
    }

    /**
     * Pantry-aware suggestions: sorted by match percentage (highest to lowest)
     * Returns maximum recipes sorted by match percentage
     */
    public List<Recipe> getPantryAwareSuggestions(PantryManager pantryManager) {
        List<String> availableIngredients = pantryManager.getAllIngredients().stream()
                .filter(i -> i.getQuantity() > 0)
                .map(Ingredient::getName)
                .collect(java.util.stream.Collectors.toList());

        // Include all recipes and sort by match percentage (descending)
        List<Recipe> suggestions = new ArrayList<>(recipes);

        // Cache match scores to avoid recalculating during sort (performance optimization)
        Map<Recipe, Double> scoreCache = new HashMap<>();
        for (Recipe recipe : suggestions) {
            scoreCache.put(recipe, calculateIngredientMatch(recipe.getIngredients(), availableIngredients));
        }

        // Sort by match percentage: highest match percentage at top, lowest at bottom
        suggestions.sort((a, b) -> {
            double as = scoreCache.get(a);
            double bs = scoreCache.get(b);
            return Double.compare(bs, as); // Descending: higher match first
        });

        return suggestions;
    }

    private boolean canCookWithPantry(Recipe recipe, PantryManager pantryManager) {
        for (String ing : recipe.getIngredients()) {
            Ingredient match = findMatchingIngredientInPantry(ing, pantryManager);
            if (match == null || match.getQuantity() <= 0) {
                return false;
            }
        }
        return true;
    }

    private Ingredient findMatchingIngredientInPantry(String recipeIngredient, PantryManager pantryManager) {
        // Use advanced ingredient matcher
        for (Ingredient pantryIng : pantryManager.getAllIngredients()) {
            if (pantryIng.getQuantity() <= 0) continue;
            if (IngredientMatcher.matches(recipeIngredient, pantryIng.getName())) {
                return pantryIng;
            }
        }
        return null;
    }

    
    
    /**
     * Calculate ingredient match score between recipe and available ingredients
     * Uses weighted scoring: exact matches = 1.0, partial = 0.7, etc.
     */
    private double calculateIngredientMatch(List<String> recipeIngredients, List<String> availableIngredients) {
        if (recipeIngredients.isEmpty()) return 0.0;
        
        double totalScore = 0.0;
        for (String recipeIngredient : recipeIngredients) {
            double bestMatch = 0.0;
            for (String available : availableIngredients) {
                double score = IngredientMatcher.calculateMatchScore(recipeIngredient, available);
                if (score > bestMatch) {
                    bestMatch = score;
                }
            }
            totalScore += bestMatch;
        }
        
        return totalScore / recipeIngredients.size();
    }
    
    /**
     * Check if recipe ingredient matches any available ingredient
     */
    private boolean hasMatchingIngredient(String recipeIngredient, List<String> availableIngredients) {
        for (String available : availableIngredients) {
            if (IngredientMatcher.matches(recipeIngredient, available)) {
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
