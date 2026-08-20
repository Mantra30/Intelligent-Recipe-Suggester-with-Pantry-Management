# PowerShell script to run the Smart Recipe Suggester
# This script attempts to run the application with available Java

Write-Host "Smart Recipe Suggester - Starting..." -ForegroundColor Green
Write-Host ""

# Check if Java is available
$javaCheck = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCheck) {
    Write-Host "Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 11 or higher and add it to your PATH" -ForegroundColor Yellow
    exit 1
}
$javaVersion = java -version 2>&1 | Select-Object -First 1

Write-Host "Java found: $javaVersion" -ForegroundColor Green

# Check if Maven is available
$mavenCheck = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenCheck) {
    Write-Host "Maven found - Using Maven to run the application" -ForegroundColor Green
    Write-Host ""
    mvn clean compile javafx:run
} else {
    Write-Host "Maven not found in PATH" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Attempting to find alternative JavaFX installation..." -ForegroundColor Yellow
    
    # Try to find JavaFX in common locations
    $javafxPaths = @(
        "$env:USERPROFILE\javafx-sdk-*\lib",
        "$env:ProgramFiles\Java\javafx-sdk-*\lib",
        ".\javafx-sdk-*\lib"
    )
    
    $javafxFound = $false
    $javafxPath = $null
    
    foreach ($path in $javafxPaths) {
        $resolved = Resolve-Path $path -ErrorAction SilentlyContinue
        if ($resolved) {
            $javafxPath = $resolved[0].Path
            Write-Host "Found JavaFX at: $javafxPath" -ForegroundColor Green
            $javafxFound = $true
            break
        }
    }
    
    if (-not $javafxFound) {
        Write-Host "JavaFX SDK not found" -ForegroundColor Red
        Write-Host "Attempting to download JavaFX SDK automatically..." -ForegroundColor Yellow
        
        $javafxVersion = "17.0.2"
        $javafxDir = Join-Path $env:USERPROFILE "javafx-sdk-$javafxVersion"
        $javafxLib = Join-Path $javafxDir "lib"
        
        if (-not (Test-Path $javafxLib)) {
            $javafxUrl = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-${javafxVersion}_windows-x64_bin-sdk.zip"
            $zipFile = Join-Path $env:TEMP "javafx-sdk.zip"
            
            try {
                Write-Host "Downloading JavaFX SDK..." -ForegroundColor Cyan
                Invoke-WebRequest -Uri $javafxUrl -OutFile $zipFile -UseBasicParsing
                
                Write-Host "Extracting JavaFX SDK..." -ForegroundColor Cyan
                Expand-Archive -Path $zipFile -DestinationPath $env:USERPROFILE -Force
                Remove-Item $zipFile
                
                Write-Host "JavaFX SDK downloaded successfully!" -ForegroundColor Green
                $javafxPath = $javafxLib
                $javafxFound = $true
            } catch {
                Write-Host "Failed to download JavaFX automatically." -ForegroundColor Red
                Write-Host "Please download JavaFX SDK manually from: https://openjfx.io/" -ForegroundColor Yellow
                Write-Host "Or install Maven and run: mvn clean compile javafx:run" -ForegroundColor Yellow
                exit 1
            }
        } else {
            $javafxPath = $javafxLib
            $javafxFound = $true
        }
    }
    
    if ($javafxFound) {
        # Run the application with found JavaFX
        Write-Host ""
        Write-Host "Starting application..." -ForegroundColor Green
        $modulePath = $javafxPath
        $classpath = "target/classes;src/main/resources"
        java --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp $classpath com.smartrecipes.Main
    }
}
