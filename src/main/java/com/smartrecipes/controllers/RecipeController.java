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

/**
 * Controller for the recipe management screen
 */
public class RecipeController implements Initializable {
    
    @FXML private VBox recipeContainer;
    @FXML private ListView<Recipe> recipesList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cuisineFilter;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private ComboBox<String> timeFilter;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button cookButton;
    @FXML private Button refreshButton;
    @FXML private Text recipeDetailsText;
    @FXML private Text ingredientsText;
    @FXML private Text stepsText;
    @FXML private Label statsLabel;
    
    private RecipeManager recipeManager;
    private PantryManager pantryManager;
    private HistoryManager historyManager;
    private ObservableList<Recipe> recipesObservableList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeManagers();
        setupUI();
        loadRecipes();
    }
    
    private void initializeManagers() {
        recipeManager = new RecipeManager();
        pantryManager = new PantryManager();
        historyManager = new HistoryManager();
        recipesObservableList = FXCollections.observableArrayList();
    }
    
    private void setupUI() {
        // Set up list view
        recipesList.setItems(recipesObservableList);
        recipesList.setCellFactory(listView -> new ListCell<Recipe>() {
            @Override
            protected void updateItem(Recipe recipe, boolean empty) {
                super.updateItem(recipe, empty);
                if (empty || recipe == null) {
                    setText(null);
                } else {
                    setText(recipe.getTitle() + " (" + recipe.getCuisine() + ") - " + recipe.getFormattedTotalTime());
                }
            }
        });
        
        // Set up filters
        setupFilters();
        
        // Set up button styles
        addButton.getStyleClass().add("action-button");
        editButton.getStyleClass().add("action-button");
        deleteButton.getStyleClass().add("action-button");
        cookButton.getStyleClass().add("cook-button");
        refreshButton.getStyleClass().add("action-button");
        
        // Set up selection listener
        recipesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showRecipeDetails(newVal);
            }
        });
    }
    
    private void setupFilters() {
        // Cuisine filter
        cuisineFilter.getItems().addAll("All Cuisines");
        cuisineFilter.setValue("All Cuisines");
        cuisineFilter.setOnAction(e -> filterRecipes());
        
        // Difficulty filter
        difficultyFilter.getItems().addAll("All Difficulties", "Easy", "Medium", "Hard");
        difficultyFilter.setValue("All Difficulties");
        difficultyFilter.setOnAction(e -> filterRecipes());
        
        // Time filter
        timeFilter.getItems().addAll("Any Time", "Under 30 min", "Under 1 hour", "Under 2 hours");
        timeFilter.setValue("Any Time");
        timeFilter.setOnAction(e -> filterRecipes());
        
        // Search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRecipes();
        });
    }
    
    private void loadRecipes() {
        Platform.runLater(() -> {
            recipesObservableList.clear();
            recipesObservableList.addAll(recipeManager.getAllRecipes());
            updateFilters();
            updateStats();
        });
    }
    
    private void updateFilters() {
        // Update cuisine filter
        String currentCuisine = cuisineFilter.getValue();
        Set<String> cuisines = recipeManager.getAllCuisines();
        
        cuisineFilter.getItems().clear();
        cuisineFilter.getItems().add("All Cuisines");
        cuisineFilter.getItems().addAll(cuisines);
        
        if (cuisines.contains(currentCuisine)) {
            cuisineFilter.setValue(currentCuisine);
        } else {
            cuisineFilter.setValue("All Cuisines");
        }
    }
    
    private void filterRecipes() {
        String searchQuery = searchField.getText();
        String selectedCuisine = cuisineFilter.getValue();
        String selectedDifficulty = difficultyFilter.getValue();
        String selectedTime = timeFilter.getValue();
        
        List<Recipe> filteredRecipes = recipeManager.getAllRecipes();
        
        // Apply search filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            filteredRecipes = recipeManager.searchRecipes(searchQuery);
        }
        
        // Apply cuisine filter
        if (selectedCuisine != null && !selectedCuisine.equals("All Cuisines")) {
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> selectedCuisine.equals(recipe.getCuisine()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply difficulty filter
        if (selectedDifficulty != null && !selectedDifficulty.equals("All Difficulties")) {
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> selectedDifficulty.equals(recipe.getDifficulty()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply time filter
        if (selectedTime != null && !selectedTime.equals("Any Time")) {
            int maxTime = getMaxTimeFromFilter(selectedTime);
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getTotalTime() <= maxTime)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        recipesObservableList.clear();
        recipesObservableList.addAll(filteredRecipes);
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
        
        // Show ingredients
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            ingredientsBuilder.append((i + 1)).append(". ").append(recipe.getIngredients().get(i)).append("\n");
        }
        ingredientsText.setText(ingredientsBuilder.toString());
        
        // Show steps
        StringBuilder stepsBuilder = new StringBuilder();
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            stepsBuilder.append((i + 1)).append(". ").append(recipe.getSteps().get(i)).append("\n\n");
        }
        stepsText.setText(stepsBuilder.toString());
    }
    
    private void updateStats() {
        Map<String, Object> stats = recipeManager.getRecipeStats();
        statsLabel.setText(String.format(
            "Total: %d | Custom: %d | Cuisines: %d | Avg Time: %d min",
            (Integer) stats.get("totalRecipes"),
            (Integer) stats.get("customRecipes"),
            (Integer) stats.get("cuisines"),
            (Integer) stats.get("averageCookingTime")
        ));
    }
    
    @FXML
    private void handleAddButton() {
        showAddEditDialog(null);
    }
    
    @FXML
    private void handleEditButton() {
        Recipe selectedRecipe = recipesList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            showAddEditDialog(selectedRecipe);
        } else {
            UIUtils.showWarningAlert("No Selection", "Please select a recipe to edit.");
        }
    }
    
    @FXML
    private void handleDeleteButton() {
        Recipe selectedRecipe = recipesList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            if (UIUtils.showConfirmationDialog("Confirm Delete", 
                    "Are you sure you want to delete '" + selectedRecipe.getTitle() + "'?")) {
                recipeManager.deleteRecipe(selectedRecipe.getId());
                loadRecipes();
                UIUtils.showInfoAlert("Success", "Recipe deleted successfully!");
            }
        } else {
            UIUtils.showWarningAlert("No Selection", "Please select a recipe to delete.");
        }
    }
    
    @FXML
    private void handleCookButton() {
        Recipe selectedRecipe = recipesList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            cookRecipe(selectedRecipe);
        } else {
            UIUtils.showWarningAlert("No Selection", "Please select a recipe to cook.");
        }
    }
    
    @FXML
    private void handleRefreshButton() {
        loadRecipes();
        UIUtils.showInfoAlert("Refreshed", "Recipe data has been updated!");
    }
    
    private void cookRecipe(Recipe recipe) {
        // Check if ingredients are available
        List<String> availableIngredients = pantryManager.getAllIngredients().stream()
                .map(Ingredient::getName)
                .collect(java.util.stream.Collectors.toList());
        
        boolean canCook = pantryManager.consumeIngredients(recipe.getIngredients());
        
        if (canCook) {
            // Add to cooking history
            historyManager.addCookingEntry(recipe.getId(), recipe.getTitle(), recipe.getServings(), "");
            
            UIUtils.showInfoAlert("Recipe Cooked!", 
                "Successfully cooked '" + recipe.getTitle() + "'!\nPantry has been updated.");
        } else {
            UIUtils.showWarningAlert("Missing Ingredients", 
                "Some ingredients are not available in your pantry.\nPlease check your pantry and try again.");
        }
    }
    
    private void showAddEditDialog(Recipe recipe) {
        Dialog<Recipe> dialog = new Dialog<>();
        dialog.setTitle(recipe == null ? "Add Recipe" : "Edit Recipe");
        
        // Create form fields
        TextField titleField = new TextField();
        TextArea descriptionField = new TextArea();
        TextArea ingredientsField = new TextArea();
        TextArea stepsField = new TextArea();
        ComboBox<String> cuisineCombo = new ComboBox<>();
        ComboBox<String> difficultyCombo = new ComboBox<>();
        TextField prepTimeField = new TextField();
        TextField cookTimeField = new TextField();
        TextField servingsField = new TextField();
        
        // Set up combo boxes
        cuisineCombo.getItems().addAll("Indian", "Italian", "Chinese", "Mexican", "Thai", "Japanese", "Other");
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        
        // Set default values for editing
        if (recipe != null) {
            titleField.setText(recipe.getTitle());
            descriptionField.setText(recipe.getDescription());
            ingredientsField.setText(String.join("\n", recipe.getIngredients()));
            stepsField.setText(String.join("\n", recipe.getSteps()));
            cuisineCombo.setValue(recipe.getCuisine());
            difficultyCombo.setValue(recipe.getDifficulty());
            prepTimeField.setText(String.valueOf(recipe.getPrepTime()));
            cookTimeField.setText(String.valueOf(recipe.getCookTime()));
            servingsField.setText(String.valueOf(recipe.getServings()));
        } else {
            cuisineCombo.setValue("Indian");
            difficultyCombo.setValue("Easy");
            prepTimeField.setText("15");
            cookTimeField.setText("30");
            servingsField.setText("4");
        }
        
        // Create form layout
        VBox form = new VBox(10);
        form.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionField,
            new Label("Cuisine:"), cuisineCombo,
            new Label("Difficulty:"), difficultyCombo,
            new Label("Prep Time (min):"), prepTimeField,
            new Label("Cook Time (min):"), cookTimeField,
            new Label("Servings:"), servingsField,
            new Label("Ingredients (one per line):"), ingredientsField,
            new Label("Steps (one per line):"), stepsField
        );
        
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Validate input
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String title = UIUtils.sanitizeInput(titleField.getText());
                String description = UIUtils.sanitizeInput(descriptionField.getText());
                String cuisine = cuisineCombo.getValue();
                String difficulty = difficultyCombo.getValue();
                
                if (title.isEmpty()) {
                    UIUtils.showErrorAlert("Invalid Input", "Title cannot be empty.");
                    return null;
                }
                
                try {
                    int prepTime = Integer.parseInt(prepTimeField.getText());
                    int cookTime = Integer.parseInt(cookTimeField.getText());
                    int servings = Integer.parseInt(servingsField.getText());
                    
                    List<String> ingredients = Arrays.asList(ingredientsField.getText().split("\n"));
                    List<String> steps = Arrays.asList(stepsField.getText().split("\n"));
                    
                    Recipe newRecipe = new Recipe(null, title, description, ingredients, steps, 
                            cuisine, difficulty, prepTime, cookTime, servings);
                    newRecipe.setCustom(true);
                    
                    if (recipe != null) {
                        newRecipe.setId(recipe.getId());
                    }
                    
                    return newRecipe;
                } catch (NumberFormatException e) {
                    UIUtils.showErrorAlert("Invalid Input", "Please enter valid numbers for time and servings.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Recipe> result = dialog.showAndWait();
        result.ifPresent(newRecipe -> {
            if (recipe == null) {
                recipeManager.addRecipe(newRecipe);
                UIUtils.showInfoAlert("Success", "Recipe added successfully!");
            } else {
                recipeManager.updateRecipe(newRecipe);
                UIUtils.showInfoAlert("Success", "Recipe updated successfully!");
            }
            loadRecipes();
        });
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
