$rootPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system"
$files = Get-ChildItem -Path $rootPath -Recurse -Include *.java, *.fxml

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    if ($content.Contains("UserData")) {
        Write-Host "Updating UserData -> PersonData in $($file.Name)"
        $content = $content.Replace("UserData", "PersonData")
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
