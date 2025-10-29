# 🍲 Smart Recipe Suggester with Pantry Management

A modern, food-themed JavaFX desktop application that suggests recipes based on available pantry ingredients and helps reduce food waste.

## ✨ Features

### 🧱 Core Functionality
- **Smart Recipe Suggestions**: AI-powered recipe recommendations based on available ingredients
- **Pantry Management**: Track ingredients with expiry dates and quantity management
- **Auto-Update Inventory**: Automatically remove used ingredients when cooking
- **Cooking History**: Track cooking frequency and favorite recipes
- **Custom Recipes**: Add your own recipes with detailed instructions

### 🎨 UI Design
- **Modern JavaFX Interface**: Clean, intuitive design with warm color palette
- **Responsive Layout**: Adapts to different screen sizes
- **Smooth Animations**: Fade, slide, and scale transitions
- **Food-Themed Icons**: Emojis and visual elements for better UX

### 🌍 Cuisine Support
- **Indian Cuisine**: Paneer Butter Masala, Dosa, Biryani, Rajma, Pav Bhaji
- **International**: Italian, Chinese, Mexican, Thai, Japanese, Middle Eastern
- **30+ Pre-loaded Recipes**: Ready-to-use recipe database

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- JavaFX 17 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smart-recipe-suggester
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

   Or use the Maven profile:
   ```bash
   mvn clean compile exec:java -P run
   ```

### Building Executable JAR

```bash
mvn clean package
java -jar target/smart-recipe-suggester-1.0.0.jar
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/smartrecipes/
│   │   ├── Main.java                    # Application entry point
│   │   ├── controllers/                 # JavaFX controllers
│   │   │   ├── DashboardController.java
│   │   │   ├── PantryController.java
│   │   │   ├── RecipeController.java
│   │   │   ├── SuggestionController.java
│   │   │   └── HistoryController.java
│   │   ├── models/                      # Core business logic
│   │   │   ├── Ingredient.java
│   │   │   ├── Recipe.java
│   │   │   ├── PantryManager.java
│   │   │   ├── RecipeManager.java
│   │   │   └── HistoryManager.java
│   │   └── utils/                       # Utility classes
│   │       ├── FileHandler.java
│   │       ├── FuzzyMatcher.java
│   │       └── UIUtils.java
│   └── resources/
│       ├── fxml/                        # FXML layouts
│       │   ├── dashboard.fxml
│       │   ├── pantry.fxml
│       │   ├── recipe.fxml
│       │   ├── suggestion.fxml
│       │   └── history.fxml
│       ├── css/                         # Styling
│       │   ├── theme.css
│       │   └── animations.css
│       └── data/                        # JSON data files
│           ├── recipes.json
│           ├── pantry.json
│           └── history.json
└── test/                                # Test files
    └── com/smartrecipes/tests/
```

## 🎯 Usage Guide

### Dashboard
- View pantry statistics and health
- See recent cooking history
- Check expiring ingredients
- Quick navigation to all features

### Pantry Management
- Add/edit/delete ingredients
- Set expiry dates and quantities
- Filter by category
- Search ingredients
- View low stock alerts

### Recipe Management
- Browse all recipes
- Add custom recipes
- Edit existing recipes
- Cook recipes (updates pantry)
- Filter by cuisine, difficulty, time

### Smart Suggestions
- Get recipe recommendations based on available ingredients
- See ingredient match percentages
- Filter suggestions
- Cook suggested recipes

### Cooking History
- View cooking statistics
- Track most cooked recipes
- See cooking trends
- Manage cooking entries

## 🛠️ Technical Details

### Architecture
- **MVC Pattern**: Clear separation of concerns
- **JavaFX**: Modern desktop UI framework
- **JSON Storage**: Local data persistence
- **Fuzzy Matching**: Smart ingredient matching algorithm

### Key Technologies
- **Java 11+**: Core programming language
- **JavaFX 17**: UI framework
- **Jackson**: JSON processing
- **Maven**: Build and dependency management

### Data Storage
- **recipes.json**: Recipe database
- **pantry.json**: Current pantry inventory
- **history.json**: Cooking history and statistics

## 🎨 Customization

### Adding New Recipes
1. Edit `src/main/resources/data/recipes.json`
2. Follow the existing JSON structure
3. Include all required fields: title, ingredients, steps, cuisine, etc.

### Modifying UI Theme
1. Edit `src/main/resources/css/theme.css`
2. Change color variables and styling
3. Add new CSS classes as needed

### Adding New Features
1. Create new controller classes in `controllers/` package
2. Add corresponding FXML files in `resources/fxml/`
3. Update navigation in `Main.java`

## 🧪 Testing

Run tests with:
```bash
mvn test
```

Test coverage includes:
- Model classes functionality
- Utility methods
- File I/O operations
- Fuzzy matching algorithms

## 📝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- JavaFX community for excellent documentation
- Jackson library for JSON processing
- Food emoji creators for the beautiful icons
- Open source community for inspiration

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Check the documentation
- Review the code comments

---

**Happy Cooking! 🍳👨‍🍳👩‍🍳**
