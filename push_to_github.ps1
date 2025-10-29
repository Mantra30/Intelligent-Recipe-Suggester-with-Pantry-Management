# PowerShell script to push repository to GitHub
# Usage: .\push_to_github.ps1 -GitHubUrl "https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git"

param(
    [Parameter(Mandatory=$true)]
    [string]$GitHubUrl
)

Write-Host "Setting up GitHub remote..." -ForegroundColor Green

# Remove existing remote if it exists
git remote remove origin 2>$null

# Add the GitHub remote
git remote add origin $GitHubUrl

# Rename branch to main (GitHub standard)
git branch -M main

Write-Host "`nPushing to GitHub..." -ForegroundColor Green
Write-Host "You may be prompted for credentials." -ForegroundColor Yellow
Write-Host "For HTTPS, use your GitHub username and a Personal Access Token." -ForegroundColor Yellow
Write-Host "For SSH, make sure your SSH key is configured." -ForegroundColor Yellow

# Push to GitHub
git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Successfully pushed to GitHub!" -ForegroundColor Green
    Write-Host "Repository URL: $($GitHubUrl -replace '\.git$','')" -ForegroundColor Cyan
} else {
    Write-Host "`n❌ Push failed. Please check the error messages above." -ForegroundColor Red
    Write-Host "Make sure:" -ForegroundColor Yellow
    Write-Host "  1. The repository exists on GitHub" -ForegroundColor Yellow
    Write-Host "  2. You have the correct permissions" -ForegroundColor Yellow
    Write-Host "  3. Your credentials are correct" -ForegroundColor Yellow
}

