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

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller for the pantry management screen
 */
public class PantryController implements Initializable {
    
    @FXML private VBox pantryContainer;
    @FXML private TableView<Ingredient> ingredientsTable;
    @FXML private TableColumn<Ingredient, String> nameColumn;
    @FXML private TableColumn<Ingredient, Double> quantityColumn;
    @FXML private TableColumn<Ingredient, String> unitColumn;
    @FXML private TableColumn<Ingredient, String> expiryColumn;
    @FXML private TableColumn<Ingredient, String> categoryColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label statsLabel;
    
    private PantryManager pantryManager;
    private ObservableList<Ingredient> ingredientsList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeManager();
        setupTable();
        setupFilters();
        loadIngredients();
    }
    
    private void initializeManager() {
        pantryManager = new PantryManager();
        ingredientsList = FXCollections.observableArrayList();
    }
    
    private void setupTable() {
        // Configure table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Custom cell factory for expiry date with color coding
        expiryColumn.setCellValueFactory(new PropertyValueFactory<>("formattedExpiryDate"));
        expiryColumn.setCellFactory(column -> new TableCell<Ingredient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    if (ingredient != null) {
                        String color = UIUtils.getExpiryColor(ingredient.getExpiryDate());
                        setStyle("-fx-text-fill: " + color + ";");
                    }
                }
            }
        });
        
        // Set table items
        ingredientsTable.setItems(ingredientsList);
        
        // Enable multi-selection
        ingredientsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Set up button styles
        addButton.getStyleClass().add("action-button");
        editButton.getStyleClass().add("action-button");
        deleteButton.getStyleClass().add("action-button");
        refreshButton.getStyleClass().add("action-button");
    }
    
    private void setupFilters() {
        // Set up category filter
        categoryFilter.getItems().addAll("All Categories");
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> filterIngredients());
        
        // Set up search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterIngredients();
        });
    }
    
    private void loadIngredients() {
        Platform.runLater(() -> {
            ingredientsList.clear();
            ingredientsList.addAll(pantryManager.getAllIngredients());
            updateCategoryFilter();
            updateStats();
        });
    }
    
    private void updateCategoryFilter() {
        String currentSelection = categoryFilter.getValue();
        Set<String> categories = pantryManager.getAllCategories();
        
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(categories);
        
        if (categories.contains(currentSelection)) {
            categoryFilter.setValue(currentSelection);
        } else {
            categoryFilter.setValue("All Categories");
        }
    }
    
    private void filterIngredients() {
        String searchQuery = searchField.getText();
        String selectedCategory = categoryFilter.getValue();
        
        List<Ingredient> filteredIngredients = pantryManager.getAllIngredients();
        
        // Apply search filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            filteredIngredients = pantryManager.searchIngredients(searchQuery);
        }
        
        // Apply category filter
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            filteredIngredients = filteredIngredients.stream()
                    .filter(ingredient -> selectedCategory.equals(ingredient.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        ingredientsList.clear();
        ingredientsList.addAll(filteredIngredients);
    }
    
    private void updateStats() {
        Map<String, Object> stats = pantryManager.getPantryStats();
        statsLabel.setText(String.format(
            "Total: %d | Expiring Soon: %d | Expired: %d | Low Stock: %d",
            (Integer) stats.get("totalIngredients"),
            (Integer) stats.get("expiringSoon"),
            (Integer) stats.get("expired"),
            (Integer) stats.get("lowStock")
        ));
    }
    
    @FXML
    private void handleAddButton() {
        showAddEditDialog(null);
    }
    
    @FXML
    private void handleEditButton() {
        Ingredient selectedIngredient = ingredientsTable.getSelectionModel().getSelectedItem();
        if (selectedIngredient != null) {
            showAddEditDialog(selectedIngredient);
        } else {
            UIUtils.showWarningAlert("No Selection", "Please select an ingredient to edit.");
        }
    }
    
    @FXML
    private void handleDeleteButton() {
        ObservableList<Ingredient> selectedIngredients = ingredientsTable.getSelectionModel().getSelectedItems();
        if (selectedIngredients.isEmpty()) {
            UIUtils.showWarningAlert("No Selection", "Please select ingredients to delete.");
            return;
        }
        
        String message = selectedIngredients.size() == 1 
            ? "Are you sure you want to delete '" + selectedIngredients.get(0).getName() + "'?"
            : "Are you sure you want to delete " + selectedIngredients.size() + " ingredients?";
            
        if (UIUtils.showConfirmationDialog("Confirm Delete", message)) {
            for (Ingredient ingredient : selectedIngredients) {
                pantryManager.removeIngredient(ingredient.getName());
            }
            loadIngredients();
            UIUtils.showInfoAlert("Success", "Selected ingredients have been deleted.");
        }
    }
    
    @FXML
    private void handleRefreshButton() {
        loadIngredients();
        UIUtils.showInfoAlert("Refreshed", "Pantry data has been updated!");
    }
    
    private void showAddEditDialog(Ingredient ingredient) {
        Dialog<Ingredient> dialog = new Dialog<>();
        dialog.setTitle(ingredient == null ? "Add Ingredient" : "Edit Ingredient");
        
        // Create form fields
        TextField nameField = new TextField();
        TextField quantityField = new TextField();
        TextField unitField = new TextField();
        DatePicker expiryDatePicker = new DatePicker();
        TextField categoryField = new TextField();
        
        // Set default values for editing
        if (ingredient != null) {
            nameField.setText(ingredient.getName());
            quantityField.setText(String.valueOf(ingredient.getQuantity()));
            unitField.setText(ingredient.getUnit());
            expiryDatePicker.setValue(ingredient.getExpiryDate());
            categoryField.setText(ingredient.getCategory());
        } else {
            unitField.setText("pieces");
            expiryDatePicker.setValue(LocalDate.now().plusDays(7));
            categoryField.setText("General");
        }
        
        // Create form layout
        VBox form = new VBox(10);
        form.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Quantity:"), quantityField,
            new Label("Unit:"), unitField,
            new Label("Expiry Date:"), expiryDatePicker,
            new Label("Category:"), categoryField
        );
        
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Validate input
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = UIUtils.sanitizeInput(nameField.getText());
                String quantityText = quantityField.getText();
                String unit = UIUtils.sanitizeInput(unitField.getText());
                LocalDate expiryDate = expiryDatePicker.getValue();
                String category = UIUtils.sanitizeInput(categoryField.getText());
                
                if (name.isEmpty()) {
                    UIUtils.showErrorAlert("Invalid Input", "Name cannot be empty.");
                    return null;
                }
                
                if (!UIUtils.isValidQuantity(quantityText)) {
                    UIUtils.showErrorAlert("Invalid Input", "Please enter a valid quantity.");
                    return null;
                }
                
                double quantity = Double.parseDouble(quantityText);
                return new Ingredient(name, quantity, unit, expiryDate, category);
            }
            return null;
        });
        
        Optional<Ingredient> result = dialog.showAndWait();
        result.ifPresent(newIngredient -> {
            pantryManager.addIngredient(newIngredient);
            loadIngredients();
            UIUtils.showInfoAlert("Success", 
                ingredient == null ? "Ingredient added successfully!" : "Ingredient updated successfully!");
        });
    }
    
    public PantryManager getPantryManager() {
        return pantryManager;
    }
}
