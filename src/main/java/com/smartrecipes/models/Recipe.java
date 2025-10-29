package com.smartrecipes.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a recipe with ingredients, steps, and metadata
 */
public class Recipe {
    private String id;
    private String title;
    private String description;
    private List<String> ingredients;
    private List<String> steps;
    private String cuisine;
    private String difficulty;
    private int prepTime; // in minutes
    private int cookTime; // in minutes
    private int servings;
    private String imagePath;
    private List<String> tags;
    private boolean isCustom;
    private String dietaryType; // vegetarian, non-vegetarian, vegan
    
    // Default constructor for JSON deserialization
    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.tags = new ArrayList<>();
    }
    
    public Recipe(String id, String title, String description, List<String> ingredients, 
                  List<String> steps, String cuisine, String difficulty, int prepTime, 
                  int cookTime, int servings) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.steps = steps != null ? steps : new ArrayList<>();
        this.cuisine = cuisine;
        this.difficulty = difficulty;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
        this.tags = new ArrayList<>();
        this.isCustom = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public int getPrepTime() { return prepTime; }
    public void setPrepTime(int prepTime) { this.prepTime = prepTime; }
    
    public int getCookTime() { return cookTime; }
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }
    
    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { this.isCustom = custom; }
    
    public String getDietaryType() { return dietaryType; }
    public void setDietaryType(String dietaryType) { this.dietaryType = dietaryType; }
    
    /**
     * Get total cooking time (prep + cook)
     */
    public int getTotalTime() {
        return prepTime + cookTime;
    }
    
    /**
     * Get formatted total time string
     */
    public String getFormattedTotalTime() {
        int total = getTotalTime();
        if (total < 60) {
            return total + " min";
        } else {
            int hours = total / 60;
            int minutes = total % 60;
            if (minutes == 0) {
                return hours + " hr";
            } else {
                return hours + " hr " + minutes + " min";
            }
        }
    }
    
    /**
     * Add an ingredient to the recipe
     */
    public void addIngredient(String ingredient) {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        ingredients.add(ingredient);
    }
    
    /**
     * Add a step to the recipe
     */
    public void addStep(String step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }
    
    /**
     * Add a tag to the recipe
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(id, recipe.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return title + " (" + cuisine + ")";
    }
}
