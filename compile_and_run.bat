@echo off
echo Compiling Smart Recipe Suggester...
echo.

REM Set JavaFX path (adjust this to your JavaFX installation)
set JAVAFX_PATH=C:\path\to\javafx-sdk\lib

REM Compile all Java files
javac -d build --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml -cp ".:lib/*" src/main/java/com/smartrecipes/*.java src/main/java/com/smartrecipes/**/*.java

echo.
echo Compilation complete. Attempting to run...
echo.

REM Copy resources to build directory
xcopy /E /I /Y src\main\resources build\resources

REM Run the application
java --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml -cp "build;lib/*" com.smartrecipes.Main

pause
