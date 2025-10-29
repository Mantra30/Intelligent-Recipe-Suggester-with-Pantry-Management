package com.smartrecipes;

import com.smartrecipes.models.*;
import com.smartrecipes.utils.UIUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Smart Recipe Suggester with advanced features
 */
public class Main extends Application {
    
    private Stage primaryStage;
    private PantryManager pantryManager;
    private RecipeManager recipeManager;
    private HistoryManager historyManager;
    
    // UI Components
    private TextField ingredientInput;
    private TextField quantityInput;
    private ComboBox<String> unitComboBox;
    private TextArea recipeDisplay;
    private ListView<String> suggestionsList;
    private ListView<String> pantryList;
    private Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("🍲 Smart Recipe Suggester - Advanced Edition");
        
        // Initialize managers
        pantryManager = new PantryManager();
        recipeManager = new RecipeManager();
        historyManager = new HistoryManager();
        
        // Create and show the main scene
        Scene scene = createMainScene();
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        System.out.println("Smart Recipe Suggester Advanced Edition started successfully! 🍲");
    }
    
    private Scene createMainScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #fdf6e3, #f4f1e8);");
        
        // Top section with title and input
        VBox topSection = createTopSection();
        root.setTop(topSection);
        
        // Center section with main content
        HBox centerSection = createCenterSection();
        root.setCenter(centerSection);
        
        // Bottom section with status
        HBox bottomSection = createBottomSection();
        root.setBottom(bottomSection);
        
        return new Scene(root, 1000, 700);
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(20));
        topSection.setAlignment(Pos.CENTER);
        topSection.setStyle("-fx-background-color: rgba(230, 126, 34, 0.1); -fx-background-radius: 10;");
        
        // Main title with enhanced font
        Text title = new Text("🍲 Smart Recipe Suggester");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        title.setStyle("-fx-fill: linear-gradient(to right, #e67e22, #f39c12); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        title.setTextAlignment(TextAlignment.CENTER);
        
        // Subtitle
        Text subtitle = new Text("Discover delicious recipes based on your available ingredients");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setStyle("-fx-fill: #34495e;");
        subtitle.setTextAlignment(TextAlignment.CENTER);
        
        // Ingredient input section
        VBox inputSection = new VBox(10);
        inputSection.setAlignment(Pos.CENTER);
        
        Label inputLabel = new Label("Add Ingredients:");
        inputLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        inputLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER);
        
        ingredientInput = new TextField();
        ingredientInput.setPromptText("Ingredient name (e.g., chicken, rice)");
        ingredientInput.setFont(Font.font("Segoe UI", 12));
        ingredientInput.setPrefWidth(200);
        ingredientInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #bdc3c7; -fx-padding: 8;");
        
        quantityInput = new TextField();
        quantityInput.setPromptText("Quantity");
        quantityInput.setFont(Font.font("Segoe UI", 12));
        quantityInput.setPrefWidth(80);
        quantityInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #bdc3c7; -fx-padding: 8;");
        
        unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("pieces", "g", "kg", "ml", "l", "cups", "tbsp", "tsp", "oz", "lb");
        unitComboBox.setValue("pieces");
        unitComboBox.setPrefWidth(80);
        unitComboBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #bdc3c7;");
        
        Button addIngredientBtn = createStyledButton("➕ Add", "#27ae60");
        addIngredientBtn.setOnAction(e -> addIngredient());
        
        inputRow.getChildren().addAll(ingredientInput, quantityInput, unitComboBox, addIngredientBtn);
        
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button suggestBtn = createStyledButton("🔍 Find Recipes", "#e67e22");
        suggestBtn.setOnAction(e -> findRecipes());
        
        Button scrapeBtn = createStyledButton("🌐 Scrape Web", "#9b59b6");
        scrapeBtn.setOnAction(e -> scrapeWebRecipes());
        
        Button browseBtn = createStyledButton("📚 Browse All", "#8e44ad");
        browseBtn.setOnAction(e -> browseAllRecipes());
        
        Button clearBtn = createStyledButton("🗑️ Clear All", "#e74c3c");
        clearBtn.setOnAction(e -> clearAll());
        
        actionButtons.getChildren().addAll(suggestBtn, scrapeBtn, browseBtn, clearBtn);
        
        inputSection.getChildren().addAll(inputLabel, inputRow, actionButtons);
        
        topSection.getChildren().addAll(title, subtitle, inputSection);
        
        return topSection;
    }
    
    private HBox createCenterSection() {
        HBox centerSection = new HBox(20);
        centerSection.setPadding(new Insets(20));
        
        // Left panel - Pantry and Suggestions
        VBox leftPanel = new VBox(15);
        leftPanel.setPrefWidth(400);
        
        // Pantry section
        VBox pantrySection = createPantrySection();
        leftPanel.getChildren().add(pantrySection);
        
        // Suggestions section
        VBox suggestionsSection = createSuggestionsSection();
        leftPanel.getChildren().add(suggestionsSection);
        
        // Right panel - Recipe display
        VBox rightPanel = new VBox(15);
        rightPanel.setPrefWidth(500);
        
        // Recipe display section
        VBox recipeSection = createRecipeSection();
        rightPanel.getChildren().add(recipeSection);
        
        centerSection.getChildren().addAll(leftPanel, rightPanel);
        
        return centerSection;
    }
    
    private VBox createPantrySection() {
        VBox pantrySection = new VBox(10);
        pantrySection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        Text pantryTitle = new Text("📦 Your Pantry");
        pantryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        pantryTitle.setStyle("-fx-fill: #27ae60;");
        
        ListView<String> pantryList = new ListView<>();
        pantryList.setPrefHeight(150);
        pantryList.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5;");
        
        // Store reference for later use
        this.pantryList = pantryList;
        
        // Populate pantry list
        updatePantryList(pantryList);
        
        HBox pantryButtons = new HBox(10);
        Button refreshPantryBtn = createStyledButton("🔄 Refresh", "#3498db");
        refreshPantryBtn.setOnAction(e -> updatePantryList(pantryList));
        
        Button removePantryBtn = createStyledButton("🗑️ Remove", "#e74c3c");
        removePantryBtn.setOnAction(e -> removeSelectedPantryItem());
        
        Button managePantryBtn = createStyledButton("⚙️ Manage", "#9b59b6");
        managePantryBtn.setOnAction(e -> showPantryManager());
        
        pantryButtons.getChildren().addAll(refreshPantryBtn, removePantryBtn, managePantryBtn);
        
        pantrySection.getChildren().addAll(pantryTitle, pantryList, pantryButtons);
        
        return pantrySection;
    }
    
    private VBox createSuggestionsSection() {
        VBox suggestionsSection = new VBox(10);
        suggestionsSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        Text suggestionsTitle = new Text("💡 Recipe Suggestions");
        suggestionsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        suggestionsTitle.setStyle("-fx-fill: #e67e22;");
        
        // Dietary legend
        HBox legendBox = new HBox(15);
        legendBox.setAlignment(Pos.CENTER);
        
        Label vegLabel = new Label("🥬 VEGETARIAN");
        vegLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        vegLabel.setFont(Font.font("Segoe UI", 11));
        
        Label veganLabel = new Label("🌱 VEGAN");
        veganLabel.setStyle("-fx-text-fill: #16a085; -fx-font-weight: bold;");
        veganLabel.setFont(Font.font("Segoe UI", 11));
        
        Label nonVegLabel = new Label("🍖 NON-VEG");
        nonVegLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        nonVegLabel.setFont(Font.font("Segoe UI", 11));
        
        legendBox.getChildren().addAll(vegLabel, veganLabel, nonVegLabel);
        
        suggestionsList = new ListView<>();
        suggestionsList.setPrefHeight(200);
        suggestionsList.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5;");
        
        // Set cell factory for custom styling with dietary colors
        suggestionsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", 13));
                    
                    // Color code based on dietary type
                    if (item.contains("🥬") || item.contains("[VEGETARIAN]") || item.contains("vegetarian")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-background-color: #d5f4e6;");
                    } else if (item.contains("🌱") || item.contains("[VEGAN]") || item.contains("vegan")) {
                        setStyle("-fx-text-fill: #16a085; -fx-font-weight: bold; -fx-background-color: #d1f2eb;");
                    } else if (item.contains("🍖") || item.contains("[NON-VEGETARIAN]") || item.contains("non-vegetarian")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-background-color: #fadbd8;");
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;");
                    }
                }
            }
        });
        
        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRecipeDetails(selected);
            }
        });
        
        suggestionsSection.getChildren().addAll(suggestionsTitle, suggestionsList);
        
        return suggestionsSection;
    }
    
    private VBox createRecipeSection() {
        VBox recipeSection = new VBox(10);
        recipeSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        Text recipeTitle = new Text("📖 Recipe Details");
        recipeTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        recipeTitle.setStyle("-fx-fill: #3498db;");
        
        recipeDisplay = new TextArea();
        recipeDisplay.setPrefHeight(400);
        recipeDisplay.setEditable(false);
        recipeDisplay.setFont(Font.font("Segoe UI", 12));
        recipeDisplay.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5; -fx-padding: 10;");
        recipeDisplay.setText("Select a recipe from suggestions to view details...");
        
        HBox recipeButtons = new HBox(10);
        Button cookBtn = createStyledButton("🍳 Cook This Recipe", "#e74c3c");
        cookBtn.setOnAction(e -> cookSelectedRecipe());
        
        Button saveBtn = createStyledButton("💾 Save Recipe", "#27ae60");
        saveBtn.setOnAction(e -> saveCurrentRecipe());
        
        recipeButtons.getChildren().addAll(cookBtn, saveBtn);
        
        recipeSection.getChildren().addAll(recipeTitle, recipeDisplay, recipeButtons);
        
        return recipeSection;
    }
    
    private HBox createBottomSection() {
        HBox bottomSection = new HBox(15);
        bottomSection.setPadding(new Insets(15));
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setStyle("-fx-background-color: rgba(52, 73, 94, 0.1); -fx-background-radius: 10;");
        
        statusLabel = new Label("Ready to discover amazing recipes! 🍽️");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        statusLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Stats display
        HBox statsBox = new HBox(20);
        
        Label pantryStats = new Label("📦 Pantry: " + pantryManager.getAllIngredients().size() + " items");
        Label recipeStats = new Label("📖 Recipes: " + recipeManager.getAllRecipes().size() + " available");
        Label historyStats = new Label("📊 History: " + historyManager.getAllHistory().size() + " sessions");
        
        for (Label stat : Arrays.asList(pantryStats, recipeStats, historyStats)) {
            stat.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            stat.setStyle("-fx-text-fill: #7f8c8d;");
        }
        
        statsBox.getChildren().addAll(pantryStats, recipeStats, historyStats);
        
        bottomSection.getChildren().addAll(statusLabel, statsBox);
        
        return bottomSection;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setStyle("-fx-background-color: " + color + "; " +
                       "-fx-text-fill: white; " +
                       "-fx-padding: 8px 16px; " +
                       "-fx-background-radius: 8px; " +
                       "-fx-border-radius: 8px; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2); " +
                       "-fx-cursor: hand;");
        
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + 
                           "-fx-scale-x: 1.05; " +
                           "-fx-scale-y: 1.05; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle()
                           .replace("-fx-scale-x: 1.05; ", "")
                           .replace("-fx-scale-y: 1.05;", "")
                           .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);", 
                                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"));
        });
        
        return button;
    }
    
    private void addIngredient() {
        String ingredientName = ingredientInput.getText().trim();
        String quantityText = quantityInput.getText().trim();
        String unit = unitComboBox.getValue();
        
        if (ingredientName.isEmpty()) {
            statusLabel.setText("Please enter an ingredient name! ⚠️");
            return;
        }
        
        if (quantityText.isEmpty()) {
            statusLabel.setText("Please enter a quantity! ⚠️");
            return;
        }
        
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) {
                statusLabel.setText("Quantity must be greater than 0! ⚠️");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid quantity! ⚠️");
            return;
        }
        
        // Add ingredient to pantry
        Ingredient ingredient = new Ingredient(
            ingredientName,
            quantity,
            unit,
            java.time.LocalDate.now().plusDays(7),
            "General"
        );
        
        pantryManager.addIngredient(ingredient);
        ingredientInput.clear();
        quantityInput.clear();
        statusLabel.setText("Added " + quantity + " " + unit + " of " + ingredientName + " to pantry! ✅");
        
        // Update pantry display
        updatePantryList(pantryList);
    }
    
    private void removeSelectedPantryItem() {
        if (pantryList == null) {
            statusLabel.setText("Pantry list not available! ⚠️");
            return;
        }
        
        String selectedItem = pantryList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            statusLabel.setText("Please select an item to remove! 👆");
            return;
        }
        
        // Extract ingredient name from display text
        String ingredientName = selectedItem.split(" \\(")[0];
        
        // Remove from pantry
        boolean removed = pantryManager.removeIngredient(ingredientName);
        
        if (removed) {
            statusLabel.setText("Removed " + ingredientName + " from pantry! ✅");
            updatePantryList(pantryList);
        } else {
            statusLabel.setText("Failed to remove " + ingredientName + "! ❌");
        }
    }
    
    private void findRecipes() {
        List<String> availableIngredients = pantryManager.getAllIngredients().stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        
        if (availableIngredients.isEmpty()) {
            statusLabel.setText("Add some ingredients first! 📦");
            return;
        }
        
        List<Recipe> suggestions = recipeManager.getSuggestedRecipes(availableIngredients, pantryManager);
        
        // Update suggestions list
        suggestionsList.getItems().clear();
        for (Recipe recipe : suggestions) {
            double matchScore = calculateMatchScore(recipe, availableIngredients);
            String dietaryIcon = getDietaryIcon(recipe.getDietaryType());
            String dietaryType = recipe.getDietaryType() != null ? recipe.getDietaryType() : "Unknown";
            String displayText = String.format("%s [%s] %s (%s) - %.0f%% match - %s", 
                dietaryIcon, dietaryType.toUpperCase(), recipe.getTitle(), recipe.getCuisine(), matchScore * 100, recipe.getFormattedTotalTime());
            suggestionsList.getItems().add(displayText);
        }
        
        statusLabel.setText("Found " + suggestions.size() + " recipe suggestions! 🎯");
    }
    
    private void browseAllRecipes() {
        List<Recipe> allRecipes = recipeManager.getAllRecipes();
        
        if (allRecipes.isEmpty()) {
            statusLabel.setText("No recipes available! Add some ingredients and scrape web recipes. 📚");
            return;
        }
        
        // Update suggestions list with all recipes
        suggestionsList.getItems().clear();
        for (Recipe recipe : allRecipes) {
            String dietaryIcon = getDietaryIcon(recipe.getDietaryType());
            String dietaryType = recipe.getDietaryType() != null ? recipe.getDietaryType() : "Unknown";
            String displayText = String.format("%s [%s] %s (%s) - %s - %s", 
                dietaryIcon, dietaryType.toUpperCase(), recipe.getTitle(), recipe.getCuisine(), recipe.getDifficulty(), recipe.getFormattedTotalTime());
            suggestionsList.getItems().add(displayText);
        }
        
        statusLabel.setText("Showing all " + allRecipes.size() + " available recipes! 📚");
    }
    
    private void scrapeWebRecipes() {
        List<String> availableIngredients = pantryManager.getAllIngredients().stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        
        if (availableIngredients.isEmpty()) {
            statusLabel.setText("Add some ingredients first to scrape recipes! 📦");
            return;
        }
        
        statusLabel.setText("Scraping recipes from web... 🌐");
        
        // Use web scraping to find recipes
        List<Recipe> scrapedRecipes = recipeManager.scrapeRecipesFromWeb(String.join(" ", availableIngredients));
        
        // Update suggestions list with scraped recipes
        suggestionsList.getItems().clear();
        for (Recipe recipe : scrapedRecipes) {
            double matchScore = calculateMatchScore(recipe, availableIngredients);
            String dietaryIcon = getDietaryIcon(recipe.getDietaryType());
            String dietaryType = recipe.getDietaryType() != null ? recipe.getDietaryType() : "Unknown";
            String displayText = String.format("%s [%s] %s (%s) - %.0f%% match - %s [WEB]", 
                dietaryIcon, dietaryType.toUpperCase(), recipe.getTitle(), recipe.getCuisine(), matchScore * 100, recipe.getFormattedTotalTime());
            suggestionsList.getItems().add(displayText);
        }
        
        statusLabel.setText("Found " + scrapedRecipes.size() + " recipes from web! 🌐");
    }
    
    private void clearAll() {
        pantryManager.getAllIngredients().clear();
        suggestionsList.getItems().clear();
        recipeDisplay.setText("Select a recipe from suggestions to view details...");
        statusLabel.setText("Cleared all data! 🗑️");
    }
    
    private void updatePantryList(ListView<String> pantryList) {
        if (pantryList != null) {
            pantryList.getItems().clear();
            for (Ingredient ingredient : pantryManager.getAllIngredients()) {
                String displayText = String.format("%s (%.1f %s) - %s", 
                    ingredient.getName(), ingredient.getQuantity(), ingredient.getUnit(), 
                    ingredient.getFormattedExpiryDate());
                pantryList.getItems().add(displayText);
            }
        }
    }
    
    private void showPantryManager() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pantry Manager");
        alert.setHeaderText("Pantry Statistics");
        
        StringBuilder content = new StringBuilder();
        content.append("Total Ingredients: ").append(pantryManager.getAllIngredients().size()).append("\n");
        
        List<Ingredient> expiringSoon = pantryManager.getExpiringIngredients(7);
        content.append("Expiring Soon (7 days): ").append(expiringSoon.size()).append("\n");
        
        List<Ingredient> expired = pantryManager.getExpiredIngredients();
        content.append("Expired: ").append(expired.size()).append("\n\n");
        
        content.append("Ingredients:\n");
        for (Ingredient ingredient : pantryManager.getAllIngredients()) {
            content.append("• ").append(ingredient.getName()).append(" (")
                   .append(ingredient.getQuantity()).append(" ").append(ingredient.getUnit()).append(")\n");
        }
        
        alert.setContentText(content.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
    
    private String getDietaryIcon(String dietaryType) {
        if (dietaryType == null) return "🍽️";
        
        switch (dietaryType.toLowerCase()) {
            case "vegetarian": return "🥬";
            case "vegan": return "🌱";
            case "non-vegetarian": return "🍖";
            default: return "🍽️";
        }
    }
    
    private void showRecipeDetails(String selectedRecipe) {
        // Extract recipe title from display text (remove dietary icon and type label)
        String recipeTitle = selectedRecipe.replaceAll("^[🥬🌱🍖🍽️] \\[.*?\\] ", "").split(" \\(")[0];
        
        Recipe recipe = recipeManager.getAllRecipes().stream()
                .filter(r -> r.getTitle().equals(recipeTitle))
                .findFirst()
                .orElse(null);
        
        if (recipe != null) {
            StringBuilder details = new StringBuilder();
            String dietaryIcon = getDietaryIcon(recipe.getDietaryType());
            String dietaryType = recipe.getDietaryType() != null ? recipe.getDietaryType() : "Unknown";
            
            details.append("🍽️ ").append(recipe.getTitle()).append(" ").append(dietaryIcon).append("\n");
            details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            
            details.append("📋 Description:\n").append(recipe.getDescription()).append("\n\n");
            
            details.append("🌍 Cuisine: ").append(recipe.getCuisine()).append("\n");
            details.append("🥗 Dietary Type: ").append(dietaryType).append(" ").append(dietaryIcon).append("\n");
            details.append("⚡ Difficulty: ").append(recipe.getDifficulty()).append("\n");
            details.append("⏱️ Prep Time: ").append(recipe.getPrepTime()).append(" min\n");
            details.append("🔥 Cook Time: ").append(recipe.getCookTime()).append(" min\n");
            details.append("👥 Servings: ").append(recipe.getServings()).append("\n\n");
            
            details.append("🥘 Ingredients:\n");
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                details.append("  ").append(i + 1).append(". ").append(recipe.getIngredients().get(i)).append("\n");
            }
            
            details.append("\n👨‍🍳 Instructions:\n");
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                details.append("  ").append(i + 1).append(". ").append(recipe.getSteps().get(i)).append("\n\n");
            }
            
            recipeDisplay.setText(details.toString());
            statusLabel.setText("Displaying recipe: " + recipe.getTitle() + " " + dietaryIcon + " 📖");
        }
    }
    
    private void cookSelectedRecipe() {
        String selected = suggestionsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a recipe first! 👆");
            return;
        }
        
        String recipeTitle = selected.split(" \\(")[0];
        Recipe recipe = recipeManager.getAllRecipes().stream()
                .filter(r -> r.getTitle().equals(recipeTitle))
                .findFirst()
                .orElse(null);
        
        if (recipe != null) {
            // Add to cooking history
            historyManager.addCookingEntry(recipe.getId(), recipe.getTitle(), recipe.getServings(), "");
            
            // Update pantry (consume ingredients)
            pantryManager.consumeIngredients(recipe.getIngredients());
            
            statusLabel.setText("Successfully cooked " + recipe.getTitle() + "! 🍳");
            
            // Refresh displays
            updatePantryList(null);
            findRecipes();
        }
    }
    
    private void saveCurrentRecipe() {
        String selected = suggestionsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a recipe first! 👆");
            return;
        }
        
        statusLabel.setText("Recipe saved to favorites! 💾");
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
            
            if (normalizedRecipe.equals(normalizedAvailable) ||
                normalizedRecipe.contains(normalizedAvailable) ||
                normalizedAvailable.contains(normalizedRecipe)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        // Save data when application closes
        if (pantryManager != null) {
            pantryManager.savePantry();
        }
        if (recipeManager != null) {
            recipeManager.saveRecipes();
        }
        if (historyManager != null) {
            historyManager.saveHistory();
        }
        
        System.out.println("Application closed successfully.");
    }
}