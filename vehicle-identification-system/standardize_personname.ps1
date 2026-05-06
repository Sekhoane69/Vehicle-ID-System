$rootPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system"
$files = Get-ChildItem -Path $rootPath -Recurse -Include *.java

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $changed = $false
    
    # Standardize 'Personname' (variable/field) to 'personname'
    # But ONLY when it's not part of a Class name or something similar
    # We'll use regex to find standalone 'Personname'
    
    if ($content -match "\bPersonname\b") {
        Write-Host "Lowercasing 'Personname' in $($file.Name)"
        $content = $content -replace "\bPersonname\b", "personname"
        $changed = $true
    }
    
    # Also standardize 'getPersonname' to 'getpersonname' etc. if they exist
    if ($content -match "\bgetPersonname\b") {
        $content = $content -replace "\bgetPersonname\b", "getpersonname"
        $changed = $true
    }
    if ($content -match "\bsetPersonname\b") {
        $content = $content -replace "\bsetPersonname\b", "setpersonname"
        $changed = $true
    }

    if ($changed) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
