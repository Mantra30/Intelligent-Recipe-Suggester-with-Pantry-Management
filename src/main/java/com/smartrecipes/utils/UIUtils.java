package com.smartrecipes.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for UI animations, alerts, and styling helpers
 */
public class UIUtils {
    
    /**
     * Show a fade-in animation for a node
     */
    public static void fadeIn(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
    
    /**
     * Show a fade-out animation for a node
     */
    public static void fadeOut(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
    }
    
    /**
     * Show a scale animation for a node
     */
    public static void scaleAnimation(Node node, double scaleX, double scaleY, Duration duration) {
        ScaleTransition scaleTransition = new ScaleTransition(duration, node);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(scaleX);
        scaleTransition.setToY(scaleY);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
    }
    
    /**
     * Show a slide-in animation from the left
     */
    public static void slideInFromLeft(Node node, Duration duration) {
        TranslateTransition translateTransition = new TranslateTransition(duration, node);
        translateTransition.setFromX(-node.getBoundsInLocal().getWidth());
        translateTransition.setToX(0);
        translateTransition.play();
    }
    
    /**
     * Show a slide-in animation from the right
     */
    public static void slideInFromRight(Node node, Duration duration) {
        TranslateTransition translateTransition = new TranslateTransition(duration, node);
        translateTransition.setFromX(node.getBoundsInLocal().getWidth());
        translateTransition.setToX(0);
        translateTransition.play();
    }
    
    /**
     * Show a slide-in animation from the top
     */
    public static void slideInFromTop(Node node, Duration duration) {
        TranslateTransition translateTransition = new TranslateTransition(duration, node);
        translateTransition.setFromY(-node.getBoundsInLocal().getHeight());
        translateTransition.setToY(0);
        translateTransition.play();
    }
    
    /**
     * Show a slide-in animation from the bottom
     */
    public static void slideInFromBottom(Node node, Duration duration) {
        TranslateTransition translateTransition = new TranslateTransition(duration, node);
        translateTransition.setFromY(node.getBoundsInLocal().getHeight());
        translateTransition.setToY(0);
        translateTransition.play();
    }
    
    /**
     * Show an information alert
     */
    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show a warning alert
     */
    public static void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show an error alert
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show a confirmation dialog
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show a text input dialog
     */
    public static Optional<String> showTextInputDialog(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        
        return dialog.showAndWait();
    }
    
    /**
     * Format a date for display
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "No date";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
    
    /**
     * Format a date for display with relative time
     */
    public static String formatDateWithRelative(LocalDate date) {
        if (date == null) return "No date";
        
        LocalDate today = LocalDate.now();
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, date);
        
        String formattedDate = formatDate(date);
        
        if (daysDiff == 0) {
            return formattedDate + " (Today)";
        } else if (daysDiff == 1) {
            return formattedDate + " (Tomorrow)";
        } else if (daysDiff == -1) {
            return formattedDate + " (Yesterday)";
        } else if (daysDiff > 0) {
            return formattedDate + " (In " + daysDiff + " days)";
        } else {
            return formattedDate + " (" + Math.abs(daysDiff) + " days ago)";
        }
    }
    
    /**
     * Get color based on expiry status
     */
    public static String getExpiryColor(LocalDate expiryDate) {
        if (expiryDate == null) return "#666666";
        
        LocalDate today = LocalDate.now();
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
        
        if (daysDiff < 0) {
            return "#e74c3c"; // Red for expired
        } else if (daysDiff <= 3) {
            return "#f39c12"; // Orange for expiring soon
        } else if (daysDiff <= 7) {
            return "#f1c40f"; // Yellow for expiring in a week
        } else {
            return "#27ae60"; // Green for fresh
        }
    }
    
    /**
     * Get difficulty color
     */
    public static String getDifficultyColor(String difficulty) {
        if (difficulty == null) return "#666666";
        
        switch (difficulty.toLowerCase()) {
            case "easy":
                return "#27ae60"; // Green
            case "medium":
                return "#f39c12"; // Orange
            case "hard":
                return "#e74c3c"; // Red
            default:
                return "#666666"; // Gray
        }
    }
    
    /**
     * Get cuisine color
     */
    public static String getCuisineColor(String cuisine) {
        if (cuisine == null) return "#666666";
        
        switch (cuisine.toLowerCase()) {
            case "indian":
                return "#e67e22"; // Orange
            case "italian":
                return "#27ae60"; // Green
            case "chinese":
                return "#e74c3c"; // Red
            case "mexican":
                return "#f39c12"; // Yellow
            case "thai":
                return "#9b59b6"; // Purple
            case "japanese":
                return "#3498db"; // Blue
            default:
                return "#95a5a6"; // Gray
        }
    }
    
    /**
     * Create a custom font
     */
    public static Font createCustomFont(String family, double size, FontWeight weight) {
        return Font.font(family, weight, size);
    }
    
    /**
     * Create a custom font with default family
     */
    public static Font createCustomFont(double size, FontWeight weight) {
        return Font.font("System", weight, size);
    }
    
    /**
     * Convert hex color to Color object
     */
    public static Color hexToColor(String hex) {
        try {
            return Color.web(hex);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Validate quantity input
     */
    public static boolean isValidQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) return false;
        try {
            double value = Double.parseDouble(quantity);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Sanitize input string
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"'&]", "");
    }
    
    /**
     * Truncate text to specified length
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Capitalize first letter of each word
     */
    public static String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        
        String[] words = text.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            if (words[i].length() > 0) {
                result.append(words[i].substring(0, 1).toUpperCase())
                      .append(words[i].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
}
