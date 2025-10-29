package com.smartrecipes.controllers;

import com.smartrecipes.models.*;
import com.smartrecipes.utils.UIUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the recipe suggestion screen
 */
public class SuggestionController implements Initializable {
    
    @FXML private VBox suggestionContainer;
    @FXML private ListView<Recipe> suggestionsList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cuisineFilter;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private ComboBox<String> timeFilter;
    @FXML private Button refreshButton;
    @FXML private Button cookButton;
    @FXML private Text recipeDetailsText;
    @FXML private Text ingredientsText;
    @FXML private Text stepsText;
    @FXML private Text matchScoreText;
    @FXML private Label statsLabel;
    
    private RecipeManager recipeManager;
    private PantryManager pantryManager;
    private HistoryManager historyManager;
    private ObservableList<Recipe> suggestionsObservableList;
    private Map<Recipe, Double> recipeMatchScores;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeManagers();
        setupUI();
        loadSuggestions();
    }
    
    private void initializeManagers() {
        recipeManager = new RecipeManager();
        pantryManager = new PantryManager();
        historyManager = new HistoryManager();
        suggestionsObservableList = FXCollections.observableArrayList();
        recipeMatchScores = new HashMap<>();
    }
    
    private void setupUI() {
        // Set up list view
        suggestionsList.setItems(suggestionsObservableList);
        suggestionsList.setCellFactory(listView -> new ListCell<Recipe>() {
            @Override
            protected void updateItem(Recipe recipe, boolean empty) {
                super.updateItem(recipe, empty);
                if (empty || recipe == null) {
                    setText(null);
                } else {
                    double matchScore = recipeMatchScores.getOrDefault(recipe, 0.0);
                    String matchPercentage = String.format("%.0f%%", matchScore * 100);
                    setText(recipe.getTitle() + " (" + recipe.getCuisine() + ") - " + 
                           recipe.getFormattedTotalTime() + " - Match: " + matchPercentage);
                }
            }
        });
        
        // Set up filters
        setupFilters();
        
        // Set up button styles
        refreshButton.getStyleClass().add("action-button");
        cookButton.getStyleClass().add("cook-button");
        
        // Set up selection listener
        suggestionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showRecipeDetails(newVal);
            }
        });
    }
    
    private void setupFilters() {
        // Cuisine filter
        cuisineFilter.getItems().addAll("All Cuisines");
        cuisineFilter.setValue("All Cuisines");
        cuisineFilter.setOnAction(e -> filterSuggestions());
        
        // Difficulty filter
        difficultyFilter.getItems().addAll("All Difficulties", "Easy", "Medium", "Hard");
        difficultyFilter.setValue("All Difficulties");
        difficultyFilter.setOnAction(e -> filterSuggestions());
        
        // Time filter
        timeFilter.getItems().addAll("Any Time", "Under 30 min", "Under 1 hour", "Under 2 hours");
        timeFilter.setValue("Any Time");
        timeFilter.setOnAction(e -> filterSuggestions());
        
        // Search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSuggestions();
        });
    }
    
    private void loadSuggestions() {
        Platform.runLater(() -> {
            List<String> availableIngredients = pantryManager.getAllIngredients().stream()
                    .map(Ingredient::getName)
                    .collect(Collectors.toList());
            
            List<Recipe> suggestions = recipeManager.getSuggestedRecipes(availableIngredients, pantryManager);
            
            // Calculate match scores
            recipeMatchScores.clear();
            for (Recipe recipe : suggestions) {
                double matchScore = calculateMatchScore(recipe, availableIngredients);
                recipeMatchScores.put(recipe, matchScore);
            }
            
            // Sort by match score
            suggestions.sort((r1, r2) -> {
                double score1 = recipeMatchScores.get(r1);
                double score2 = recipeMatchScores.get(r2);
                return Double.compare(score2, score1);
            });
            
            suggestionsObservableList.clear();
            suggestionsObservableList.addAll(suggestions);
            
            updateFilters();
            updateStats();
        });
    }
    
    private double calculateMatchScore(Recipe recipe, List<String> availableIngredients) {
        if (recipe.getIngredients().isEmpty()) return 0.0;
        
        int matches = 0;
        for (String recipeIngredient : recipe.getIngredients()) {
            if (hasMatchingIngredient(recipeIngredient, availableIngredients)) {
                matches++;
            }
        }
        
        return (double) matches / recipe.getIngredients().size();
    }
    
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
        }
        
        return false;
    }
    
    private void filterSuggestions() {
        String searchQuery = searchField.getText();
        String selectedCuisine = cuisineFilter.getValue();
        String selectedDifficulty = difficultyFilter.getValue();
        String selectedTime = timeFilter.getValue();
        
        List<Recipe> filteredSuggestions = new ArrayList<>(suggestionsObservableList);
        
        // Apply search filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            filteredSuggestions = filteredSuggestions.stream()
                    .filter(recipe -> recipe.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                    (recipe.getDescription() != null && 
                                     recipe.getDescription().toLowerCase().contains(searchQuery.toLowerCase())))
                    .collect(Collectors.toList());
        }
        
        // Apply cuisine filter
        if (selectedCuisine != null && !selectedCuisine.equals("All Cuisines")) {
            filteredSuggestions = filteredSuggestions.stream()
                    .filter(recipe -> selectedCuisine.equals(recipe.getCuisine()))
                    .collect(Collectors.toList());
        }
        
        // Apply difficulty filter
        if (selectedDifficulty != null && !selectedDifficulty.equals("All Difficulties")) {
            filteredSuggestions = filteredSuggestions.stream()
                    .filter(recipe -> selectedDifficulty.equals(recipe.getDifficulty()))
                    .collect(Collectors.toList());
        }
        
        // Apply time filter
        if (selectedTime != null && !selectedTime.equals("Any Time")) {
            int maxTime = getMaxTimeFromFilter(selectedTime);
            filteredSuggestions = filteredSuggestions.stream()
                    .filter(recipe -> recipe.getTotalTime() <= maxTime)
                    .collect(Collectors.toList());
        }
        
        suggestionsObservableList.clear();
        suggestionsObservableList.addAll(filteredSuggestions);
    }
    
    private int getMaxTimeFromFilter(String timeFilter) {
        switch (timeFilter) {
            case "Under 30 min": return 30;
            case "Under 1 hour": return 60;
            case "Under 2 hours": return 120;
            default: return Integer.MAX_VALUE;
        }
    }
    
    private void showRecipeDetails(Recipe recipe) {
        double matchScore = recipeMatchScores.getOrDefault(recipe, 0.0);
        String matchPercentage = String.format("%.0f%%", matchScore * 100);
        
        recipeDetailsText.setText(String.format(
            "Title: %s\nCuisine: %s\nDifficulty: %s\nPrep Time: %d min\nCook Time: %d min\nTotal Time: %s\nServings: %d",
            recipe.getTitle(),
            recipe.getCuisine(),
            recipe.getDifficulty(),
            recipe.getPrepTime(),
            recipe.getCookTime(),
            recipe.getFormattedTotalTime(),
            recipe.getServings()
        ));
        
        matchScoreText.setText("Ingredient Match: " + matchPercentage);
        
        // Show ingredients with availability status
        StringBuilder ingredientsBuilder = new StringBuilder();
        List<String> availableIngredients = pantryManager.getAllIngredients().stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            String ingredient = recipe.getIngredients().get(i);
            boolean available = hasMatchingIngredient(ingredient, availableIngredients);
            String status = available ? "✓" : "✗";
            ingredientsBuilder.append(status).append(" ").append((i + 1)).append(". ").append(ingredient).append("\n");
        }
        ingredientsText.setText(ingredientsBuilder.toString());
        
        // Show steps
        StringBuilder stepsBuilder = new StringBuilder();
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            stepsBuilder.append((i + 1)).append(". ").append(recipe.getSteps().get(i)).append("\n\n");
        }
        stepsText.setText(stepsBuilder.toString());
    }
    
    private void updateFilters() {
        // Update cuisine filter
        String currentCuisine = cuisineFilter.getValue();
        Set<String> cuisines = suggestionsObservableList.stream()
                .map(Recipe::getCuisine)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        cuisineFilter.getItems().clear();
        cuisineFilter.getItems().add("All Cuisines");
        cuisineFilter.getItems().addAll(cuisines);
        
        if (cuisines.contains(currentCuisine)) {
            cuisineFilter.setValue(currentCuisine);
        } else {
            cuisineFilter.setValue("All Cuisines");
        }
    }
    
    private void updateStats() {
        int totalSuggestions = suggestionsObservableList.size();
        int highMatchSuggestions = (int) suggestionsObservableList.stream()
                .mapToDouble(recipe -> recipeMatchScores.getOrDefault(recipe, 0.0))
                .filter(score -> score >= 0.8)
                .count();
        
        statsLabel.setText(String.format(
            "Suggestions: %d | High Match (80%%+): %d | Available Ingredients: %d",
            totalSuggestions,
            highMatchSuggestions,
            pantryManager.getAllIngredients().size()
        ));
    }
    
    @FXML
    private void handleCookButton() {
        Recipe selectedRecipe = suggestionsList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            cookRecipe(selectedRecipe);
        } else {
            UIUtils.showWarningAlert("No Selection", "Please select a recipe to cook.");
        }
    }
    
    @FXML
    private void handleRefreshButton() {
        loadSuggestions();
        UIUtils.showInfoAlert("Refreshed", "Suggestions have been updated!");
    }
    
    private void cookRecipe(Recipe recipe) {
        // Check if ingredients are available
        boolean canCook = pantryManager.consumeIngredients(recipe.getIngredients());
        
        if (canCook) {
            // Add to cooking history
            historyManager.addCookingEntry(recipe.getId(), recipe.getTitle(), recipe.getServings(), "");
            
            UIUtils.showInfoAlert("Recipe Cooked!", 
                "Successfully cooked '" + recipe.getTitle() + "'!\nPantry has been updated.");
            
            // Refresh suggestions
            loadSuggestions();
        } else {
            UIUtils.showWarningAlert("Missing Ingredients", 
                "Some ingredients are not available in your pantry.\nPlease check your pantry and try again.");
        }
    }
    
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    public PantryManager getPantryManager() {
        return pantryManager;
    }
    
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
}
