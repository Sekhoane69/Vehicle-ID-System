$allFixes = @{
    "vehicles" = "Cars"
    "vehicle_id" = "Car_id"
    "customers" = "Clients"
    "customer_id" = "Client_id"
    "users" = "Persons"
    "user_id" = "Person_id"
    "username" = "Personname"
    "violations" = "Tickets"
    "violation_id" = "Ticket_id"
    "notifications" = "Alerts"
    "notification_id" = "Alert_id"
    "customer_queries" = "Client_queries"
    "police_reports" = "police_logs"
    "service_records" = "garage_logs"
    "service_requests" = "bookings"
    "insurance_records" = "insurance_logs"
}

$rootPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system"

# 1. Update tables.sql
$sqlPath = Join-Path $rootPath "database\tables.sql"
$sqlContent = Get-Content $sqlPath -Raw
foreach ($old in $allFixes.Keys) {
    $new = $allFixes[$old]
    $sqlContent = $sqlContent -replace $old, $new
}
Set-Content -Path $sqlPath -Value $sqlContent -NoNewline

# 2. Update Java code
$javaFiles = Get-ChildItem -Path $rootPath -Recurse -Filter *.java

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $changed = $false
    foreach ($old in $allFixes.Keys) {
        $new = $allFixes[$old]
        if ($content.Contains($old)) {
            $content = $content.Replace($old, $new)
            $changed = $true
        }
    }
    if ($changed) {
        Write-Host "Updating SQL in $($file.Name)"
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
