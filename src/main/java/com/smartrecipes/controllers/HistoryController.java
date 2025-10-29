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
 * Controller for the cooking history screen
 */
public class HistoryController implements Initializable {
    
    @FXML private VBox historyContainer;
    @FXML private ListView<HistoryManager.CookingEntry> historyList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> recipeFilter;
    @FXML private ComboBox<String> timeFilter;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Text statsText;
    @FXML private Text trendsText;
    @FXML private Text topRecipesText;
    @FXML private Label statsLabel;
    
    private HistoryManager historyManager;
    private RecipeManager recipeManager;
    private ObservableList<HistoryManager.CookingEntry> historyObservableList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeManagers();
        setupUI();
        loadHistory();
    }
    
    private void initializeManagers() {
        historyManager = new HistoryManager();
        recipeManager = new RecipeManager();
        historyObservableList = FXCollections.observableArrayList();
    }
    
    private void setupUI() {
        // Set up list view
        historyList.setItems(historyObservableList);
        historyList.setCellFactory(listView -> new ListCell<HistoryManager.CookingEntry>() {
            @Override
            protected void updateItem(HistoryManager.CookingEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    setText(entry.getRecipeTitle() + " - " + entry.getFormattedDate() + 
                           " (" + entry.getServings() + " servings)");
                }
            }
        });
        
        // Set up filters
        setupFilters();
        
        // Set up button styles
        refreshButton.getStyleClass().add("action-button");
        deleteButton.getStyleClass().add("action-button");
        clearButton.getStyleClass().add("danger-button");
        
        // Enable multi-selection
        historyList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    private void setupFilters() {
        // Recipe filter
        recipeFilter.getItems().addAll("All Recipes");
        recipeFilter.setValue("All Recipes");
        recipeFilter.setOnAction(e -> filterHistory());
        
        // Time filter
        timeFilter.getItems().addAll("All Time", "Last 7 days", "Last 30 days", "Last 3 months");
        timeFilter.setValue("All Time");
        timeFilter.setOnAction(e -> filterHistory());
        
        // Search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterHistory();
        });
    }
    
    private void loadHistory() {
        Platform.runLater(() -> {
            historyObservableList.clear();
            historyObservableList.addAll(historyManager.getAllHistory());
            updateFilters();
            updateStats();
            updateTrends();
            updateTopRecipes();
        });
    }
    
    private void updateFilters() {
        // Update recipe filter
        String currentRecipe = recipeFilter.getValue();
        Set<String> recipes = historyObservableList.stream()
                .map(HistoryManager.CookingEntry::getRecipeTitle)
                .collect(Collectors.toSet());
        
        recipeFilter.getItems().clear();
        recipeFilter.getItems().add("All Recipes");
        recipeFilter.getItems().addAll(recipes);
        
        if (recipes.contains(currentRecipe)) {
            recipeFilter.setValue(currentRecipe);
        } else {
            recipeFilter.setValue("All Recipes");
        }
    }
    
    private void filterHistory() {
        String searchQuery = searchField.getText();
        String selectedRecipe = recipeFilter.getValue();
        String selectedTime = timeFilter.getValue();
        
        List<HistoryManager.CookingEntry> filteredHistory = historyManager.getAllHistory();
        
        // Apply search filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            filteredHistory = filteredHistory.stream()
                    .filter(entry -> entry.getRecipeTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                    (entry.getNotes() != null && 
                                     entry.getNotes().toLowerCase().contains(searchQuery.toLowerCase())))
                    .collect(Collectors.toList());
        }
        
        // Apply recipe filter
        if (selectedRecipe != null && !selectedRecipe.equals("All Recipes")) {
            filteredHistory = filteredHistory.stream()
                    .filter(entry -> selectedRecipe.equals(entry.getRecipeTitle()))
                    .collect(Collectors.toList());
        }
        
        // Apply time filter
        if (selectedTime != null && !selectedTime.equals("All Time")) {
            filteredHistory = filteredHistory.stream()
                    .filter(entry -> isWithinTimeRange(entry, selectedTime))
                    .collect(Collectors.toList());
        }
        
        // Sort by date (most recent first)
        filteredHistory.sort((e1, e2) -> e2.getCookedAt().compareTo(e1.getCookedAt()));
        
        historyObservableList.clear();
        historyObservableList.addAll(filteredHistory);
    }
    
    private boolean isWithinTimeRange(HistoryManager.CookingEntry entry, String timeFilter) {
        long daysDiff = entry.getDaysSinceCooking();
        
        switch (timeFilter) {
            case "Last 7 days": return daysDiff <= 7;
            case "Last 30 days": return daysDiff <= 30;
            case "Last 3 months": return daysDiff <= 90;
            default: return true;
        }
    }
    
    private void updateStats() {
        Map<String, Object> stats = historyManager.getCookingStats();
        statsText.setText(String.format(
            "Total Sessions: %d\nUnique Recipes: %d\nAverage Servings: %.1f\nCurrent Streak: %d days",
            (Integer) stats.get("totalCookingSessions"),
            (Integer) stats.get("uniqueRecipesCooked"),
            (Double) stats.get("averageServings"),
            historyManager.getCurrentCookingStreak()
        ));
    }
    
    private void updateTrends() {
        Map<String, Integer> monthlyTrends = historyManager.getCookingTrendsByMonth();
        
        if (monthlyTrends.isEmpty()) {
            trendsText.setText("No cooking trends available");
            return;
        }
        
        StringBuilder trendsBuilder = new StringBuilder("Monthly Trends:\n");
        monthlyTrends.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByKey().reversed())
                .limit(6)
                .forEach(entry -> {
                    trendsBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(" times\n");
                });
        
        trendsText.setText(trendsBuilder.toString());
    }
    
    private void updateTopRecipes() {
        List<Map.Entry<String, Integer>> topRecipes = historyManager.getMostCookedRecipes(5);
        
        if (topRecipes.isEmpty()) {
            topRecipesText.setText("No cooking history available");
            return;
        }
        
        StringBuilder topRecipesBuilder = new StringBuilder("Most Cooked Recipes:\n");
        for (int i = 0; i < topRecipes.size(); i++) {
            Map.Entry<String, Integer> entry = topRecipes.get(i);
            topRecipesBuilder.append((i + 1)).append(". ").append(entry.getKey())
                           .append(" (").append(entry.getValue()).append(" times)\n");
        }
        
        topRecipesText.setText(topRecipesBuilder.toString());
    }
    
    @FXML
    private void handleRefreshButton() {
        loadHistory();
        UIUtils.showInfoAlert("Refreshed", "History data has been updated!");
    }
    
    @FXML
    private void handleDeleteButton() {
        ObservableList<HistoryManager.CookingEntry> selectedEntries = historyList.getSelectionModel().getSelectedItems();
        if (selectedEntries.isEmpty()) {
            UIUtils.showWarningAlert("No Selection", "Please select entries to delete.");
            return;
        }
        
        String message = selectedEntries.size() == 1 
            ? "Are you sure you want to delete this cooking entry?"
            : "Are you sure you want to delete " + selectedEntries.size() + " cooking entries?";
            
        if (UIUtils.showConfirmationDialog("Confirm Delete", message)) {
            for (HistoryManager.CookingEntry entry : selectedEntries) {
                historyManager.deleteCookingEntry(entry.getId());
            }
            loadHistory();
            UIUtils.showInfoAlert("Success", "Selected entries have been deleted.");
        }
    }
    
    @FXML
    private void handleClearButton() {
        if (UIUtils.showConfirmationDialog("Clear All History", 
                "Are you sure you want to clear all cooking history? This action cannot be undone.")) {
            historyManager.clearHistory();
            loadHistory();
            UIUtils.showInfoAlert("Success", "All cooking history has been cleared.");
        }
    }
    
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
    
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
