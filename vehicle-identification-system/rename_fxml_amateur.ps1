$fxmlPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system\src\main\resources\com\vis\fxml"
$javaPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system\src\main\java"

$renames = @{
    "admin_screen.fxml" = "admin.fxml"
    "client_screen.fxml" = "client.fxml"
    "garage_screen.fxml" = "workshop.fxml"
    "home_screen.fxml" = "dashboard.fxml"
    "insurance_screen.fxml" = "insurance.fxml"
    "login_screen.fxml" = "login.fxml"
    "main_screen.fxml" = "main.fxml"
    "police_screen.fxml" = "police.fxml"
    "profile_screen.fxml" = "profile.fxml"
    "signup_screen.fxml" = "signup.fxml"
}

# Rename FXML files
foreach ($old in $renames.Keys) {
    $oldPath = Join-Path $fxmlPath $old
    $newPath = Join-Path $fxmlPath $renames[$old]
    if (Test-Path $oldPath) {
        Write-Host "Renaming $old -> $($renames[$old])"
        Rename-Item -Path $oldPath -NewName $renames[$old]
    }
}

# Update Java code references
$javaFiles = Get-ChildItem -Path $javaPath -Recurse -Include *.java
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $changed = $false
    foreach ($old in $renames.Keys) {
        if ($content.Contains($old)) {
            $new = $renames[$old]
            Write-Host "Updating reference $old -> $new in $($file.Name)"
            $content = $content.Replace($old, $new)
            $changed = $true
        }
    }
    if ($changed) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
