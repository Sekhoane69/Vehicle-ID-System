$mappings = @{
    "User" = "Person"
    "Vehicle" = "Car"
    "Customer" = "Client"
    "Violation" = "Ticket"
    "Notification" = "Alert"
    "username" = "personname" # Added this specifically
}

$rootPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system"

# Files to process
$extensions = "*.java", "*.fxml", "*.sql", "*.css"
$files = Get-ChildItem -Path $rootPath -Recurse -Include $extensions

foreach ($file in $files) {
    Write-Host "Processing $($file.FullName)"
    $content = Get-Content $file.FullName -Raw
    $changed = $false
    
    foreach ($old in $mappings.Keys) {
        $new = $mappings[$old]
        
        # Use regex for case-insensitive replacement of the whole term
        # We want to match 'user', 'User', 'USER' and replace with 'person', 'Person', 'PERSON' etc.
        # But for simplicity and 'amateur' feel, let's just do a case-insensitive string replace
        # and we can try to preserve case if we want, but amateur usually doesn't care.
        
        # However, to avoid 'personname' vs 'Personname', let's be slightly smarter.
        
        $pattern = [regex]::Escape($old)
        if ($content -match "(?i)$pattern") {
            # Case-insensitive replacement
            $content = [regex]::Replace($content, $pattern, $new, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
            $changed = $true
        }
    }
    
    if ($changed) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
