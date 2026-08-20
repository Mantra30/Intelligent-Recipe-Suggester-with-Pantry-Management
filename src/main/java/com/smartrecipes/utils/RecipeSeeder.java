package com.smartrecipes.utils;

import com.smartrecipes.models.Recipe;
import com.smartrecipes.models.RecipeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RecipeSeeder {

    private static final List<String> INDIAN_DISHES = Arrays.asList(
            "Paneer Butter Masala", "Chana Masala", "Dal Tadka", "Palak Paneer", "Aloo Gobi",
            "Rajma Masala", "Masala Dosa", "Idli Sambar", "Chicken Tikka Masala", "Butter Chicken",
            "Chicken Biryani", "Vegetable Biryani", "Veg Pulao", "Jeera Rice", "Kadhi Pakora",
            "Bhindi Masala", "Baingan Bharta", "Matar Paneer", "Malai Kofta", "Korma",
            "Tandoori Chicken", "Fish Curry", "Prawn Masala", "Egg Curry", "Methi Malai Matar",
            "Kadai Paneer", "Paneer Tikka", "Veg Korma", "Sambar", "Rasam",
            "Vada Pav", "Pav Bhaji", "Pani Puri", "Bhel Puri", "Sev Puri",
            "Poha", "Upma", "Khichdi", "Dhokla", "Thepla",
            "Chole Bhature", "Paratha", "Aloo Paratha", "Gajar Halwa", "Kheer",
            "Gulab Jamun", "Raita", "Tamarind Rice", "Lemon Rice", "Curd Rice"
    );

    private static final List<String> INDIAN_INGREDIENTS = Arrays.asList(
            "onion", "tomato", "ginger", "garlic", "green chili", "cumin seeds", "coriander powder",
            "turmeric", "red chili powder", "garam masala", "coriander leaves", "mustard seeds",
            "curry leaves", "yogurt", "ghee", "oil", "paneer", "chickpeas", "lentils",
            "spinach", "potato", "cauliflower", "peas", "rice", "basmati rice", "wheat flour"
    );

    private static final List<String> INTERNATIONAL_DISHES = Arrays.asList(
            "Spaghetti Carbonara", "Margherita Pizza", "Veggie Pizza", "Mushroom Risotto", "Caesar Salad",
            "Greek Salad", "Chicken Alfredo Pasta", "Tomato Basil Soup", "Minestrone", "French Onion Soup",
            "Beef Tacos", "Vegetarian Tacos", "Chicken Fajitas", "Falafel Wrap", "Hummus Bowl",
            "Shakshuka", "Bibimbap", "Ramen", "Sushi Bowl", "Pad Thai",
            "Stir Fry Noodles", "Teriyaki Chicken", "Quinoa Salad", "Avocado Toast", "Pancakes",
            "Waffles", "Omelette", "Grilled Cheese", "BLT Sandwich", "Burrito Bowl"
    );

    private static final List<String> COMMON_MEASURES = Arrays.asList(
            "1 cup", "2 cups", "1/2 cup", "1 tsp", "2 tsp", "1 tbsp", "2 tbsp", "to taste"
    );

    private static final Random RANDOM = new Random(42);

    public static int ensureMinimumRecipes(RecipeManager manager, int minIndian, int minInternational) {
        List<Recipe> existing = manager.getAllRecipes();
        Set<String> existingTitles = new HashSet<>();
        for (Recipe r : existing) existingTitles.add(r.getTitle().toLowerCase());

        int added = 0;
        added += seedIndian(manager, minIndian, existingTitles);
        added += seedInternational(manager, minInternational, existingTitles);
        return added;
    }

    private static int seedIndian(RecipeManager manager, int target, Set<String> existingTitles) {
        int have = countByCuisine(manager.getAllRecipes(), "Indian");
        int toAdd = Math.max(0, target - have);
        int added = 0;
        if (toAdd == 0) return 0;

        List<String> dishes = new ArrayList<>(INDIAN_DISHES);
        while (added < toAdd) {
            String base = dishes.get(RANDOM.nextInt(dishes.size()));
            String variant = variantSuffix(added);
            String title = base + variant;
            if (existingTitles.contains(title.toLowerCase())) continue;

            List<String> ingredients = pickIngredients(INDIAN_INGREDIENTS, 8, 12);
            List<String> steps = defaultSteps(base);

            Recipe recipe = new Recipe("seed_ind_" + System.currentTimeMillis() + "_" + added,
                    title,
                    base + " with classic Indian spices",
                    ingredients,
                    steps,
                    "Indian",
                    difficultyForCount(added),
                    15 + RANDOM.nextInt(20),
                    15 + RANDOM.nextInt(30),
                    4);
            manager.addRecipe(recipe);
            existingTitles.add(title.toLowerCase());
            added++;
        }
        return added;
    }

    private static int seedInternational(RecipeManager manager, int target, Set<String> existingTitles) {
        int have = countInternational(manager.getAllRecipes());
        int toAdd = Math.max(0, target - have);
        int added = 0;
        if (toAdd == 0) return 0;

        List<String> dishes = new ArrayList<>(INTERNATIONAL_DISHES);
        while (added < toAdd) {
            String base = dishes.get(RANDOM.nextInt(dishes.size()));
            String variant = variantSuffix(added);
            String title = base + variant;
            if (existingTitles.contains(title.toLowerCase())) continue;

            List<String> pool = new ArrayList<>();
            pool.addAll(Arrays.asList("olive oil", "garlic", "onion", "tomato", "basil", "oregano", "butter",
                    "parmesan", "flour", "egg", "milk", "lemon", "pepper", "salt"));
            List<String> ingredients = pickIngredients(pool, 7, 11);
            List<String> steps = defaultSteps(base);

            String cuisine = guessCuisine(base);
            Recipe recipe = new Recipe("seed_int_" + System.currentTimeMillis() + "_" + added,
                    title,
                    base + " made with accessible pantry staples",
                    ingredients,
                    steps,
                    cuisine,
                    difficultyForCount(added),
                    10 + RANDOM.nextInt(20),
                    10 + RANDOM.nextInt(25),
                    4);
            manager.addRecipe(recipe);
            existingTitles.add(title.toLowerCase());
            added++;
        }
        return added;
    }

    private static List<String> pickIngredients(List<String> source, int min, int max) {
        int n = min + RANDOM.nextInt(Math.max(1, max - min + 1));
        List<String> copy = new ArrayList<>(source);
        Collections.shuffle(copy, RANDOM);
        List<String> picked = copy.subList(0, Math.min(n, copy.size()));
        List<String> out = new ArrayList<>();
        for (String p : picked) {
            out.add(COMMON_MEASURES.get(RANDOM.nextInt(COMMON_MEASURES.size())) + " " + p);
        }
        return out;
    }

    private static List<String> defaultSteps(String base) {
        List<String> steps = new ArrayList<>();
        steps.add("Prep ingredients for " + base + ".");
        steps.add("Heat pan and sauté aromatics until fragrant.");
        steps.add("Add remaining ingredients and cook until done.");
        steps.add("Adjust seasoning and serve hot.");
        return steps;
    }

    private static String variantSuffix(int idx) {
        int mod = (idx % 5) + 1;
        return " - Variant " + mod + " #" + (idx + 1);
    }

    private static String difficultyForCount(int idx) {
        int mod = idx % 3;
        if (mod == 0) return "Easy";
        if (mod == 1) return "Medium";
        return "Hard";
    }

    private static int countByCuisine(List<Recipe> recipes, String cuisine) {
        int c = 0;
        for (Recipe r : recipes) if (cuisine.equalsIgnoreCase(r.getCuisine())) c++;
        return c;
    }

    private static int countInternational(List<Recipe> recipes) {
        int c = 0;
        for (Recipe r : recipes) {
            String cu = r.getCuisine();
            if (cu == null) continue;
            if (!"Indian".equalsIgnoreCase(cu)) c++;
        }
        return c;
    }

    private static String guessCuisine(String dish) {
        String d = dish.toLowerCase();
        if (d.contains("pizza") || d.contains("pasta") || d.contains("risotto")) return "Italian";
        if (d.contains("tacos") || d.contains("fajitas") || d.contains("burrito")) return "Mexican";
        if (d.contains("ramen") || d.contains("sushi")) return "Japanese";
        if (d.contains("bibimbap")) return "Korean";
        if (d.contains("falafel") || d.contains("hummus") || d.contains("shakshuka")) return "Middle Eastern";
        if (d.contains("caesar") || d.contains("salad")) return "International";
        return "International";
    }
}


