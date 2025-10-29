package com.smartrecipes.utils;

import com.smartrecipes.models.Recipe;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Web scraper for recipe data from various sources
 */
public class RecipeScraper {
    
    private static final String[] RECIPE_SOURCES = {
        "https://www.allrecipes.com",
        "https://www.foodnetwork.com",
        "https://www.bbcgoodfood.com",
        "https://www.tasty.co"
    };
    
    /**
     * Enhanced recipe scraping with better ingredient matching
     */
    public static List<Recipe> scrapeRecipes(String searchQuery) {
        List<Recipe> recipes = new ArrayList<>();
        
        try {
            // For demo purposes, we'll create sample recipes based on search query
            recipes.addAll(createSampleRecipes(searchQuery));
            
            // Add generic recipes that work with common ingredients
            recipes.addAll(getGenericRecipes());
            
            // Add popular recipes that are commonly searched
            recipes.addAll(getPopularRecipes());
            
        } catch (Exception e) {
            System.err.println("Error scraping recipes: " + e.getMessage());
        }
        
        return recipes;
    }
    
    /**
     * Get popular recipes that are commonly requested
     */
    private static List<Recipe> getPopularRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // Popular Italian recipes
        Recipe spaghettiCarbonara = new Recipe("popular_001", "Spaghetti Carbonara",
            "Classic Italian pasta with eggs, cheese, and pancetta",
            List.of("400g spaghetti", "200g pancetta", "4 eggs", "1 cup parmesan", "Black pepper", "Salt"),
            List.of("Cook spaghetti", "Fry pancetta", "Beat eggs with cheese", "Combine hot pasta with egg mixture", "Serve immediately"),
            "Italian", "Medium", 10, 15, 4);
        spaghettiCarbonara.setDietaryType("non-vegetarian");
        recipes.add(spaghettiCarbonara);
        
        // Popular American recipes
        Recipe burger = new Recipe("popular_002", "Classic Burger",
            "Juicy beef burger with lettuce, tomato, and cheese",
            List.of("500g ground beef", "4 burger buns", "Lettuce", "Tomatoes", "Cheese", "Onions", "Ketchup", "Mustard"),
            List.of("Form beef into patties", "Grill burgers", "Toast buns", "Assemble with toppings", "Serve with fries"),
            "American", "Easy", 15, 20, 4);
        burger.setDietaryType("non-vegetarian");
        recipes.add(burger);
        
        // Popular Indian recipes
        Recipe dal = new Recipe("popular_003", "Dal Tadka",
            "Spiced lentil curry with aromatic tempering",
            List.of("1 cup yellow lentils", "1 onion", "2 tomatoes", "Garlic", "Ginger", "Cumin seeds", "Turmeric", "Red chili powder"),
            List.of("Boil lentils", "Sauté onions and tomatoes", "Add spices", "Combine with lentils", "Add tempering"),
            "Indian", "Easy", 10, 25, 4);
        dal.setDietaryType("vegetarian");
        recipes.add(dal);
        
        return recipes;
    }
    
    /**
     * Create sample recipes based on search query
     */
    private static List<Recipe> createSampleRecipes(String searchQuery) {
        List<Recipe> recipes = new ArrayList<>();
        
        String query = searchQuery.toLowerCase();
        
        // Pizza recipes - enhanced detection
        if (query.contains("pizza") || query.contains("dough") || query.contains("cheese") || 
            query.contains("tomato") || query.contains("flour") || query.contains("yeast")) {
            
            Recipe margheritaPizza = new Recipe("scraped_pizza_001", "Margherita Pizza",
                "Classic Italian pizza with tomato sauce, mozzarella, and fresh basil",
                List.of("2 cups flour", "1 tsp yeast", "1 cup warm water", "1/2 cup tomato sauce", "200g mozzarella", "Fresh basil", "2 tbsp olive oil"),
                List.of("Make pizza dough with flour, yeast, and water", "Let dough rise for 1 hour", "Roll out dough", "Add tomato sauce", "Add mozzarella", "Bake at 450°F for 12-15 minutes", "Garnish with basil"),
                "Italian", "Medium", 30, 20, 4);
            margheritaPizza.setDietaryType("vegetarian");
            recipes.add(margheritaPizza);
            
            Recipe pepperoniPizza = new Recipe("scraped_pizza_002", "Pepperoni Pizza",
                "Classic American pizza with pepperoni and cheese",
                List.of("2 cups flour", "1 tsp yeast", "1 cup warm water", "1/2 cup tomato sauce", "200g mozzarella", "100g pepperoni", "2 tbsp olive oil"),
                List.of("Make pizza dough", "Let rise for 1 hour", "Roll out dough", "Add sauce and cheese", "Add pepperoni", "Bake at 450°F for 12-15 minutes"),
                "American", "Medium", 30, 20, 4);
            pepperoniPizza.setDietaryType("non-vegetarian");
            recipes.add(pepperoniPizza);
            
            Recipe veggiePizza = new Recipe("scraped_pizza_003", "Vegetable Supreme Pizza",
                "Loaded vegetarian pizza with fresh vegetables",
                List.of("2 cups flour", "1 tsp yeast", "1 cup warm water", "1/2 cup tomato sauce", "200g mozzarella", "1 bell pepper", "1 onion", "Mushrooms", "Olives"),
                List.of("Make dough and let rise", "Roll out dough", "Add sauce", "Add cheese", "Top with vegetables", "Bake at 450°F for 15 minutes"),
                "Italian", "Easy", 25, 20, 4);
            veggiePizza.setDietaryType("vegetarian");
            recipes.add(veggiePizza);
        }
        
        // International cuisines - French
        if (query.contains("french") || query.contains("wine") || query.contains("butter")) {
            Recipe coqAuVin = new Recipe("scraped_french_001", "Coq au Vin",
                "Classic French chicken braised in red wine",
                List.of("1 whole chicken", "2 cups red wine", "200g bacon", "1 onion", "2 carrots", "Mushrooms", "Fresh herbs"),
                List.of("Brown chicken pieces", "Cook bacon and vegetables", "Add wine and herbs", "Simmer for 1 hour", "Serve with crusty bread"),
                "French", "Hard", 30, 90, 6);
            coqAuVin.setDietaryType("non-vegetarian");
            recipes.add(coqAuVin);
        }
        
        // Spanish cuisine
        if (query.contains("spanish") || query.contains("paella") || query.contains("saffron")) {
            Recipe paella = new Recipe("scraped_spanish_001", "Seafood Paella",
                "Traditional Spanish rice dish with seafood and saffron",
                List.of("2 cups rice", "1 pinch saffron", "300g shrimp", "200g mussels", "1 bell pepper", "1 onion", "Garlic", "Olive oil"),
                List.of("Sauté vegetables", "Add rice and saffron", "Add seafood", "Cook until rice is tender", "Serve with lemon wedges"),
                "Spanish", "Medium", 20, 30, 4);
            paella.setDietaryType("non-vegetarian");
            recipes.add(paella);
        }
        
        // German cuisine
        if (query.contains("german") || query.contains("sausage") || query.contains("beer")) {
            Recipe bratwurst = new Recipe("scraped_german_001", "Bratwurst with Sauerkraut",
                "Traditional German sausage with fermented cabbage",
                List.of("4 bratwurst sausages", "1 cup sauerkraut", "1 onion", "2 potatoes", "Mustard", "Beer"),
                List.of("Boil sausages in beer", "Cook sauerkraut with onions", "Boil potatoes", "Serve with mustard"),
                "German", "Easy", 15, 25, 4);
            bratwurst.setDietaryType("non-vegetarian");
            recipes.add(bratwurst);
        }
        
        // Korean cuisine
        if (query.contains("korean") || query.contains("kimchi") || query.contains("gochujang")) {
            Recipe bibimbap = new Recipe("scraped_korean_001", "Bibimbap",
                "Korean mixed rice bowl with vegetables and gochujang",
                List.of("2 cups rice", "Carrots", "Spinach", "Bean sprouts", "Mushrooms", "Gochujang", "Sesame oil", "Fried egg"),
                List.of("Cook rice", "Prepare vegetables", "Arrange in bowl", "Add gochujang", "Top with fried egg"),
                "Korean", "Medium", 20, 15, 2);
            bibimbap.setDietaryType("vegetarian");
            recipes.add(bibimbap);
        }
        
        // Mexican cuisine
        if (query.contains("mexican") || query.contains("taco") || query.contains("chili")) {
            Recipe tacos = new Recipe("scraped_mexican_001", "Beef Tacos",
                "Authentic Mexican tacos with seasoned beef",
                List.of("400g ground beef", "8 taco shells", "1 onion", "2 tomatoes", "Lettuce", "Cheese", "Sour cream", "Taco seasoning"),
                List.of("Cook ground beef with seasoning", "Warm taco shells", "Prepare toppings", "Fill shells", "Serve with salsa"),
                "Mexican", "Easy", 15, 15, 4);
            tacos.setDietaryType("non-vegetarian");
            recipes.add(tacos);
            
            Recipe vegTacos = new Recipe("scraped_mexican_002", "Vegetarian Tacos",
                "Plant-based tacos with beans and vegetables",
                List.of("1 can black beans", "8 taco shells", "1 avocado", "2 tomatoes", "Lettuce", "Cheese", "Sour cream", "Cumin"),
                List.of("Mash beans with spices", "Warm shells", "Prepare vegetables", "Fill shells", "Serve with guacamole"),
                "Mexican", "Easy", 10, 10, 4);
            vegTacos.setDietaryType("vegetarian");
            recipes.add(vegTacos);
        }
        
        // Japanese cuisine
        if (query.contains("japanese") || query.contains("sushi") || query.contains("miso")) {
            Recipe misoSoup = new Recipe("scraped_japanese_001", "Miso Soup",
                "Traditional Japanese soup with tofu and seaweed",
                List.of("4 cups dashi stock", "3 tbsp miso paste", "200g tofu", "Wakame seaweed", "Green onions", "Soy sauce"),
                List.of("Heat dashi stock", "Dissolve miso paste", "Add tofu and seaweed", "Garnish with green onions"),
                "Japanese", "Easy", 5, 10, 4);
            misoSoup.setDietaryType("vegetarian");
            recipes.add(misoSoup);
        }
        
        // Middle Eastern cuisine
        if (query.contains("middle eastern") || query.contains("hummus") || query.contains("falafel")) {
            Recipe hummus = new Recipe("scraped_middle_east_001", "Classic Hummus",
                "Creamy chickpea dip with tahini and lemon",
                List.of("1 can chickpeas", "3 tbsp tahini", "2 cloves garlic", "1 lemon", "Olive oil", "Cumin", "Salt"),
                List.of("Drain chickpeas", "Blend with tahini", "Add garlic and lemon", "Drizzle with olive oil", "Serve with pita"),
                "Middle Eastern", "Easy", 10, 5, 6);
            hummus.setDietaryType("vegan");
            recipes.add(hummus);
        }
        
        // Chicken recipes
        if (query.contains("chicken")) {
            Recipe chickenTikka = new Recipe("scraped_001", "Chicken Tikka Masala",
                "Creamy Indian curry with tender chicken pieces",
                List.of("500g chicken breast", "1 cup yogurt", "2 onions", "3 tomatoes", "1 cup cream", "2 tbsp garam masala"),
                List.of("Marinate chicken in yogurt and spices", "Cook chicken until tender", "Make tomato-based sauce", "Add cream and simmer"),
                "Indian", "Medium", 20, 30, 4);
            chickenTikka.setDietaryType("non-vegetarian");
            recipes.add(chickenTikka);
            
            Recipe chickenStirFry = new Recipe("scraped_002", "Chicken Stir Fry",
                "Quick and healthy Asian-style chicken with vegetables",
                List.of("400g chicken breast", "2 bell peppers", "1 onion", "2 cloves garlic", "2 tbsp soy sauce", "1 tbsp sesame oil"),
                List.of("Cut chicken into strips", "Heat oil in wok", "Stir-fry chicken", "Add vegetables", "Season with soy sauce"),
                "Asian", "Easy", 15, 15, 3);
            chickenStirFry.setDietaryType("non-vegetarian");
            recipes.add(chickenStirFry);
        }
        
        // Rice recipes
        if (query.contains("rice")) {
            Recipe vegFriedRice = new Recipe("scraped_003", "Vegetable Fried Rice",
                "Colorful fried rice with mixed vegetables",
                List.of("2 cups cooked rice", "1 cup mixed vegetables", "2 eggs", "3 cloves garlic", "2 tbsp soy sauce", "1 tbsp sesame oil"),
                List.of("Heat oil in large pan", "Scramble eggs", "Add vegetables", "Add rice", "Season with soy sauce"),
                "Asian", "Easy", 10, 15, 4);
            vegFriedRice.setDietaryType("vegetarian");
            recipes.add(vegFriedRice);
            
            Recipe mushroomRisotto = new Recipe("scraped_004", "Mushroom Risotto",
                "Creamy Italian rice dish with mushrooms",
                List.of("1 cup arborio rice", "200g mushrooms", "1 onion", "1/2 cup white wine", "4 cups chicken stock", "1/2 cup parmesan"),
                List.of("Sauté onions and mushrooms", "Add rice and wine", "Gradually add stock", "Stir until creamy", "Add parmesan"),
                "Italian", "Medium", 15, 25, 4);
            mushroomRisotto.setDietaryType("vegetarian");
            recipes.add(mushroomRisotto);
        }
        
        // Pasta recipes
        if (query.contains("pasta") || query.contains("noodles")) {
            Recipe mushroomPasta = new Recipe("scraped_005", "Creamy Mushroom Pasta",
                "Rich and creamy pasta with sautéed mushrooms",
                List.of("400g pasta", "300g mushrooms", "2 cloves garlic", "1 cup cream", "1/2 cup parmesan", "Fresh herbs"),
                List.of("Cook pasta according to package", "Sauté mushrooms and garlic", "Add cream", "Combine with pasta", "Garnish with herbs"),
                "Italian", "Easy", 10, 20, 4);
            mushroomPasta.setDietaryType("vegetarian");
            recipes.add(mushroomPasta);
        }
        
        // Vegetarian recipes
        if (query.contains("vegetarian") || query.contains("veggie")) {
            Recipe vegCurry = new Recipe("scraped_006", "Vegetable Curry",
                "Spicy and aromatic vegetable curry",
                List.of("2 cups mixed vegetables", "1 onion", "3 tomatoes", "2 tbsp curry powder", "1 cup coconut milk", "Fresh cilantro"),
                List.of("Sauté onions", "Add curry powder", "Add vegetables", "Pour coconut milk", "Simmer until tender"),
                "Indian", "Easy", 15, 25, 4);
            vegCurry.setDietaryType("vegetarian");
            recipes.add(vegCurry);
        }
        
        // Vegan recipes
        if (query.contains("vegan")) {
            Recipe veganSoup = new Recipe("scraped_007", "Vegan Lentil Soup",
                "Hearty and nutritious lentil soup",
                List.of("1 cup red lentils", "2 carrots", "2 celery stalks", "1 onion", "3 cloves garlic", "4 cups vegetable stock", "Fresh herbs"),
                List.of("Sauté vegetables", "Add lentils and stock", "Simmer for 25 minutes", "Season with herbs", "Serve hot"),
                "International", "Easy", 10, 25, 4);
            veganSoup.setDietaryType("vegan");
            recipes.add(veganSoup);
        }
        
        // Default recipes if no specific match
        if (recipes.isEmpty()) {
            Recipe vegSoup = new Recipe("scraped_008", "Quick Vegetable Soup",
                "Healthy and comforting vegetable soup",
                List.of("2 cups mixed vegetables", "1 onion", "3 cloves garlic", "4 cups vegetable stock", "Fresh herbs", "Salt and pepper"),
                List.of("Sauté onions and garlic", "Add vegetables", "Pour stock", "Simmer for 20 minutes", "Season and serve"),
                "International", "Easy", 10, 20, 4);
            vegSoup.setDietaryType("vegetarian");
            recipes.add(vegSoup);
        }
        
        return recipes;
    }
    
    /**
     * Make HTTP request to a URL
     */
    private static String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes());
        }
    }
    
    /**
     * Extract recipe data from HTML content
     */
    private static Recipe extractRecipeFromHtml(String html, String sourceUrl) {
        // This is a simplified example - real implementation would be more complex
        
        String title = extractTitle(html);
        String description = extractDescription(html);
        List<String> ingredients = extractIngredients(html);
        List<String> steps = extractSteps(html);
        
        if (title != null && !ingredients.isEmpty()) {
            return new Recipe(
                "scraped_" + System.currentTimeMillis(),
                title,
                description != null ? description : "Recipe from " + sourceUrl,
                ingredients,
                steps,
                "International",
                "Medium",
                15,
                30,
                4
            );
        }
        
        return null;
    }
    
    private static String extractTitle(String html) {
        Pattern pattern = Pattern.compile("<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private static String extractDescription(String html) {
        Pattern pattern = Pattern.compile("<meta[^>]*name=\"description\"[^>]*content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private static List<String> extractIngredients(String html) {
        List<String> ingredients = new ArrayList<>();
        
        // Look for common ingredient patterns
        Pattern pattern = Pattern.compile("<li[^>]*>([^<]+)</li>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            String ingredient = matcher.group(1).trim();
            if (isValidIngredient(ingredient)) {
                ingredients.add(ingredient);
            }
        }
        
        return ingredients;
    }
    
    private static List<String> extractSteps(String html) {
        List<String> steps = new ArrayList<>();
        
        // Look for step patterns
        Pattern pattern = Pattern.compile("<p[^>]*>([^<]+)</p>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            String step = matcher.group(1).trim();
            if (isValidStep(step)) {
                steps.add(step);
            }
        }
        
        return steps;
    }
    
    private static boolean isValidIngredient(String text) {
        // Simple validation for ingredients
        return text.length() > 3 && text.length() < 100 && 
               !text.toLowerCase().contains("copyright") &&
               !text.toLowerCase().contains("privacy");
    }
    
    private static boolean isValidStep(String text) {
        // Simple validation for cooking steps
        return text.length() > 10 && text.length() < 200 &&
               !text.toLowerCase().contains("cookie") &&
               !text.toLowerCase().contains("policy");
    }
    
    /**
     * Search for recipes by ingredients
     */
    public static List<Recipe> searchRecipesByIngredients(List<String> ingredients) {
        List<Recipe> recipes = new ArrayList<>();
        
        // Create search query from ingredients
        String searchQuery = String.join(" ", ingredients);
        
        // Scrape recipes based on ingredients
        recipes.addAll(scrapeRecipes(searchQuery));
        
        // Add some generic recipes
        recipes.addAll(getGenericRecipes());
        
        return recipes;
    }
    
    /**
     * Get generic recipes that work with common ingredients
     */
    private static List<Recipe> getGenericRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        recipes.add(new Recipe("generic_001", "Simple Stir Fry",
            "Quick and easy stir fry with any vegetables",
            List.of("Mixed vegetables", "Garlic", "Soy sauce", "Oil", "Salt", "Pepper"),
            List.of("Heat oil in pan", "Add garlic", "Add vegetables", "Season with soy sauce", "Cook until tender"),
            "Asian", "Easy", 5, 10, 2));
        
        recipes.add(new Recipe("generic_002", "Vegetable Soup",
            "Hearty soup with any available vegetables",
            List.of("Mixed vegetables", "Onion", "Garlic", "Stock", "Herbs", "Salt"),
            List.of("Sauté onions", "Add vegetables", "Pour stock", "Simmer", "Season"),
            "International", "Easy", 10, 20, 4));
        
        return recipes;
    }
}
