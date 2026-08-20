package com.smartrecipes.utils;

import java.util.*;

/**
 * Advanced ingredient matching with synonyms and normalization
 */
public class IngredientMatcher {
    
    private static final Map<String, Set<String>> SYNONYMS = new HashMap<>();
    
    static {
        // Common ingredient synonyms
        addSynonyms("tomato", "tomatoes", "tomatos");
        addSynonyms("onion", "onions");
        addSynonyms("garlic", "garlic cloves", "garlic clove");
        addSynonyms("chicken", "chicken breast", "chicken meat");
        addSynonyms("rice", "basmati rice", "white rice", "brown rice");
        addSynonyms("flour", "wheat flour", "maida", "all purpose flour");
        addSynonyms("oil", "vegetable oil", "cooking oil", "olive oil");
        addSynonyms("salt", "table salt", "sea salt");
        addSynonyms("pepper", "black pepper", "peppercorn");
        addSynonyms("sugar", "white sugar", "granulated sugar");
        addSynonyms("milk", "whole milk", "cow milk");
        addSynonyms("butter", "unsalted butter", "salted butter");
        addSynonyms("egg", "eggs");
        addSynonyms("potato", "potatoes");
        addSynonyms("chili", "chili pepper", "green chili", "red chili");
        addSynonyms("ginger", "ginger root", "fresh ginger");
        addSynonyms("turmeric", "turmeric powder");
        addSynonyms("cumin", "cumin seeds", "jeera");
        addSynonyms("coriander", "coriander leaves", "cilantro", "dhania");
        addSynonyms("paneer", "cottage cheese", "indian cheese");
        addSynonyms("lentil", "lentils", "dal", "daal");
        addSynonyms("chickpea", "chickpeas", "garbanzo", "chana");
        addSynonyms("spinach", "palak", "fresh spinach");
        addSynonyms("cauliflower", "cauliflower florets", "gobi");
        addSynonyms("bell pepper", "capsicum", "sweet pepper");
        addSynonyms("mushroom", "mushrooms", "button mushroom");
        addSynonyms("yogurt", "curd", "dahi", "plain yogurt");
        addSynonyms("cheese", "cheddar cheese", "mozzarella cheese");
        addSynonyms("chicken", "chicken pieces", "chicken meat");
        addSynonyms("fish", "fish fillet", "fish pieces");
        addSynonyms("pasta", "spaghetti", "noodles", "macaroni");
    }
    
    private static void addSynonyms(String... words) {
        Set<String> synonymSet = new HashSet<>(Arrays.asList(words));
        for (String word : words) {
            SYNONYMS.put(normalize(word), synonymSet);
        }
    }
    
    /**
     * Normalize ingredient name for matching
     */
    public static String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        
        String s = raw.toLowerCase().trim();
        
        // Remove punctuation
        s = s.replaceAll("[.,()*/-]", " ");
        
        // Remove numbers and measurements
        s = s.replaceAll("\\d+[./]?\\d*", " ");
        s = s.replaceAll("\\d+", " ");
        
        // Remove common units
        String[] units = {
            "g", "kg", "gram", "grams", "gramme", "grammes",
            "ml", "l", "liter", "litre", "liters", "litres",
            "tbsp", "tbs", "tablespoon", "tablespoons",
            "tsp", "teaspoon", "teaspoons",
            "cup", "cups", "c",
            "oz", "ounce", "ounces",
            "lb", "pound", "pounds",
            "piece", "pieces", "pc", "pcs",
            "slice", "slices",
            "clove", "cloves",
            "head", "heads",
            "bunch", "bunches",
            "pack", "packs", "package", "packages",
            "can", "cans", "tin", "tins",
            "bottle", "bottles",
            "bag", "bags",
            "box", "boxes"
        };
        
        for (String unit : units) {
            s = s.replaceAll("\\b" + unit + "s?\\b", " ");
        }
        
        // Remove extra whitespace
        s = s.replaceAll("\\s+", " ").trim();
        
        return s;
    }
    
    /**
     * Check if two ingredients match (with synonyms)
     */
    public static boolean matches(String ingredient1, String ingredient2) {
        if (ingredient1 == null || ingredient2 == null) return false;
        
        String norm1 = normalize(ingredient1);
        String norm2 = normalize(ingredient2);
        
        if (norm1.isEmpty() || norm2.isEmpty()) return false;
        
        // Exact match after normalization
        if (norm1.equals(norm2)) return true;
        
        // Check if one contains the other (after normalization)
        if (norm1.contains(norm2) || norm2.contains(norm1)) {
            // Only match if the longer string is at most 2x the shorter
            int len1 = norm1.length();
            int len2 = norm2.length();
            if (len1 > 0 && len2 > 0) {
                double ratio = Math.max(len1, len2) / (double) Math.min(len1, len2);
                if (ratio <= 2.0) return true;
            }
        }
        
        // Check synonyms
        Set<String> syn1 = SYNONYMS.get(norm1);
        Set<String> syn2 = SYNONYMS.get(norm2);
        
        if (syn1 != null && syn2 != null) {
            // Check if synonym sets overlap
            Set<String> intersection = new HashSet<>(syn1);
            intersection.retainAll(syn2);
            if (!intersection.isEmpty()) return true;
            
            // Check if normalized form is in opposite synonym set
            if (syn1.contains(norm2) || syn2.contains(norm1)) return true;
        } else if (syn1 != null) {
            if (syn1.contains(norm2)) return true;
        } else if (syn2 != null) {
            if (syn2.contains(norm1)) return true;
        }
        
        // Token-based matching
        String[] tokens1 = norm1.split("\\s+");
        String[] tokens2 = norm2.split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(tokens1));
        Set<String> set2 = new HashSet<>(Arrays.asList(tokens2));
        
        // Remove common stop words
        set1.removeAll(Arrays.asList("fresh", "dried", "chopped", "sliced", "diced", "minced", "grated", "whole"));
        set2.removeAll(Arrays.asList("fresh", "dried", "chopped", "sliced", "diced", "minced", "grated", "whole"));
        
        if (set1.isEmpty() || set2.isEmpty()) return false;
        
        // Calculate token overlap
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        double overlap = intersection.size() / (double) Math.max(set1.size(), set2.size());
        
        // Match if significant overlap (>= 60%)
        return overlap >= 0.6;
    }
    
    /**
     * Calculate match score between recipe ingredient and pantry ingredient
     */
    public static double calculateMatchScore(String recipeIngredient, String pantryIngredient) {
        if (matches(recipeIngredient, pantryIngredient)) {
            return 1.0;
        }
        
        String norm1 = normalize(recipeIngredient);
        String norm2 = normalize(pantryIngredient);
        
        if (norm1.isEmpty() || norm2.isEmpty()) return 0.0;
        
        // Partial match score
        if (norm1.contains(norm2) || norm2.contains(norm1)) {
            return 0.7;
        }
        
        // Token overlap score
        String[] tokens1 = norm1.split("\\s+");
        String[] tokens2 = norm2.split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(tokens1));
        Set<String> set2 = new HashSet<>(Arrays.asList(tokens2));
        
        set1.removeAll(Arrays.asList("fresh", "dried", "chopped", "sliced", "diced", "minced", "grated"));
        set2.removeAll(Arrays.asList("fresh", "dried", "chopped", "sliced", "diced", "minced", "grated"));
        
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        return intersection.size() / (double) Math.max(set1.size(), set2.size());
    }
}










