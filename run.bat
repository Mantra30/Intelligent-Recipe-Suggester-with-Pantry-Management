@echo off
echo Setting up Smart Recipe Suggester...
echo.

REM Create build directory
if not exist build mkdir build

REM Set JavaFX path
set JAVAFX_PATH=%~dp0javafx-sdk-17.0.2\lib

echo Compiling Java files...
javac -d build --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "src/main/java" src/main/java/com/smartrecipes/*.java src/main/java/com/smartrecipes/controllers/*.java src/main/java/com/smartrecipes/models/*.java src/main/java/com/smartrecipes/utils/*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Copying resources...
xcopy /E /I /Y src\main\resources build\resources

echo.
echo Running Smart Recipe Suggester...
echo.
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "build" com.smartrecipes.Main

pause
