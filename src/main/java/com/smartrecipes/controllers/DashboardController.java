package com.smartrecipes.controllers;

import com.smartrecipes.models.*;
import com.smartrecipes.utils.UIUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller for the main dashboard screen
 */
public class DashboardController implements Initializable {
    
    @FXML private VBox dashboardContainer;
    @FXML private Text welcomeText;
    @FXML private Text pantryStatsText;
    @FXML private Text recipeStatsText;
    @FXML private Text historyStatsText;
    @FXML private Button pantryButton;
    @FXML private Button recipesButton;
    @FXML private Button suggestionsButton;
    @FXML private Button historyButton;
    @FXML private ListView<String> recentRecipesList;
    @FXML private ListView<String> expiringIngredientsList;
    @FXML private ProgressBar pantryHealthBar;
    
    private PantryManager pantryManager;
    private RecipeManager recipeManager;
    private HistoryManager historyManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeManagers();
        setupUI();
        loadDashboardData();
    }
    
    private void initializeManagers() {
        pantryManager = new PantryManager();
        recipeManager = new RecipeManager();
        historyManager = new HistoryManager();
    }
    
    private void setupUI() {
        // Set up button styles
        pantryButton.getStyleClass().add("dashboard-button");
        recipesButton.getStyleClass().add("dashboard-button");
        suggestionsButton.getStyleClass().add("dashboard-button");
        historyButton.getStyleClass().add("dashboard-button");
        
        // Set up welcome text
        welcomeText.setText("Welcome to Smart Recipe Suggester! 🍲");
        
        // Set up lists
        recentRecipesList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("list-item");
                }
            }
        });
        
        expiringIngredientsList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("list-item");
                }
            }
        });
    }
    
    private void loadDashboardData() {
        Platform.runLater(() -> {
            loadPantryStats();
            loadRecipeStats();
            loadHistoryStats();
            loadRecentRecipes();
            loadExpiringIngredients();
            updatePantryHealth();
        });
    }
    
    private void loadPantryStats() {
        Map<String, Object> stats = pantryManager.getPantryStats();
        pantryStatsText.setText(String.format(
            "Pantry: %d ingredients | %d expiring soon | %d expired",
            (Integer) stats.get("totalIngredients"),
            (Integer) stats.get("expiringSoon"),
            (Integer) stats.get("expired")
        ));
    }
    
    private void loadRecipeStats() {
        Map<String, Object> stats = recipeManager.getRecipeStats();
        recipeStatsText.setText(String.format(
            "Recipes: %d total | %d custom | %d cuisines",
            (Integer) stats.get("totalRecipes"),
            (Integer) stats.get("customRecipes"),
            (Integer) stats.get("cuisines")
        ));
    }
    
    private void loadHistoryStats() {
        Map<String, Object> stats = historyManager.getCookingStats();
        historyStatsText.setText(String.format(
            "Cooking: %d sessions | %d unique recipes | %d day streak",
            (Integer) stats.get("totalCookingSessions"),
            (Integer) stats.get("uniqueRecipesCooked"),
            historyManager.getCurrentCookingStreak()
        ));
    }
    
    private void loadRecentRecipes() {
        List<HistoryManager.CookingEntry> recentHistory = historyManager.getRecentHistory(5);
        ObservableList<String> recentRecipes = FXCollections.observableArrayList();
        
        for (HistoryManager.CookingEntry entry : recentHistory) {
            recentRecipes.add(entry.getRecipeTitle() + " - " + entry.getFormattedDate());
        }
        
        if (recentRecipes.isEmpty()) {
            recentRecipes.add("No recent cooking history");
        }
        
        recentRecipesList.setItems(recentRecipes);
    }
    
    private void loadExpiringIngredients() {
        List<Ingredient> expiringIngredients = pantryManager.getExpiringIngredients(7);
        ObservableList<String> expiringList = FXCollections.observableArrayList();
        
        for (Ingredient ingredient : expiringIngredients) {
            expiringList.add(ingredient.getName() + " - " + ingredient.getFormattedExpiryDate());
        }
        
        if (expiringList.isEmpty()) {
            expiringList.add("No ingredients expiring soon");
        }
        
        expiringIngredientsList.setItems(expiringList);
    }
    
    private void updatePantryHealth() {
        List<Ingredient> expiredIngredients = pantryManager.getExpiredIngredients();
        List<Ingredient> expiringSoon = pantryManager.getExpiringIngredients(3);
        List<Ingredient> lowStock = pantryManager.getLowStockIngredients(2.0);
        
        int totalIssues = expiredIngredients.size() + expiringSoon.size() + lowStock.size();
        int totalIngredients = pantryManager.getAllIngredients().size();
        
        if (totalIngredients == 0) {
            pantryHealthBar.setProgress(1.0);
        } else {
            double healthScore = Math.max(0.0, 1.0 - (double) totalIssues / totalIngredients);
            pantryHealthBar.setProgress(healthScore);
        }
    }
    
    @FXML
    private void handlePantryButton() {
        // This will be handled by the main application controller
        System.out.println("Navigate to Pantry");
    }
    
    @FXML
    private void handleRecipesButton() {
        System.out.println("Navigate to Recipes");
    }
    
    @FXML
    private void handleSuggestionsButton() {
        System.out.println("Navigate to Suggestions");
    }
    
    @FXML
    private void handleHistoryButton() {
        System.out.println("Navigate to History");
    }
    
    @FXML
    private void handleRefreshButton() {
        loadDashboardData();
        UIUtils.showInfoAlert("Dashboard Refreshed", "All data has been updated!");
    }
    
    public PantryManager getPantryManager() {
        return pantryManager;
    }
    
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
}
