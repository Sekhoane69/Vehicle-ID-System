$fixes = @{
    'fx:id="newPersonnameField"' = 'fx:id="newUsernameField"'
    'fx:id="newPersonField"' = 'fx:id="newUsernameField"'
    'fx:id="PersonnameField"' = 'fx:id="usernameField"'
    'fx:id="PersonsTable"' = 'fx:id="usersTable"'
    'fx:id="PersonIdCol"' = 'fx:id="userIdCol"'
    'fx:id="PersonNameCol"' = 'fx:id="userNameCol"'
    'fx:id="PersonEmailCol"' = 'fx:id="userEmailCol"'
    'fx:id="PersonRoleCol"' = 'fx:id="userRoleCol"'
    'fx:id="PersonActiveCol"' = 'fx:id="userActiveCol"'
    'fx:id="PersonCreatedCol"' = 'fx:id="userCreatedCol"'
    'fx:id="PersonsByRoleChart"' = 'fx:id="usersByRoleChart"'
    'fx:id="createPersonBtn"' = 'fx:id="createUserBtn"'
    'fx:id="updatePersonBtn"' = 'fx:id="updateUserBtn"'
    'fx:id="deletePersonBtn"' = 'fx:id="deleteUserBtn"'
    'fx:id="togglePersonBtn"' = 'fx:id="toggleUserBtn"'
    'fx:id="ClientsTable"' = 'fx:id="customersTable"'
    'fx:id="ClientsCard"' = 'fx:id="customersCard"'
    'fx:id="totalClientsLabel"' = 'fx:id="totalCustomersLabel"'
    'fx:id="CarsTable"' = 'fx:id="vehiclesTable"'
    'fx:id="totalCarsLabel"' = 'fx:id="totalVehiclesLabel"'
    'fx:id="CarProgress"' = 'fx:id="vehicleProgress"'
    'fx:id="CarsByMakeChart"' = 'fx:id="vehiclesByMakeChart"'
    'fx:id="saveCarBtn"' = 'fx:id="saveVehicleBtn"'
    'fx:id="updateCarBtn"' = 'fx:id="updateVehicleBtn"'
    'fx:id="deleteCarBtn"' = 'fx:id="deleteVehicleBtn"'
    'fx:id="TicketsCard"' = 'fx:id="violationsCard"'
    'fx:id="totalTicketsLabel"' = 'fx:id="totalViolationsLabel"'
    'fx:id="TicketsChartBox"' = 'fx:id="violationsChartBox"'
    'fx:id="TicketsFineChart"' = 'fx:id="violationsFineChart"'
    'fx:id="ClientListsContainer"' = 'fx:id="customerListsContainer"'
    'fx:id="ClientServicesListView"' = 'fx:id="customerServicesListView"'
    'fx:id="ClientTicketsListView"' = 'fx:id="customerViolationsListView"'
    'fx:id="policeAlertsListView"' = 'fx:id="policeNotificationsListView"'
    'fx:id="myCarsListView"' = 'fx:id="myVehiclesListView"'
    'fx:id="CarDetailPanel"' = 'fx:id="vehicleDetailPanel"'
    'fx:id="CarDetailTitle"' = 'fx:id="vehicleDetailTitle"'
    'fx:id="CarTicketsListView"' = 'fx:id="vehicleViolationsListView"'
    'fx:id="myCarsCombo"' = 'fx:id="myVehiclesCombo"'
}

$rootPath = "c:\Users\sekho\Downloads\Matlotlo\vehicle-identification-system\src\main\resources\com\vis\fxml"

$files = Get-ChildItem -Path $rootPath -Filter *.fxml -Recurse

foreach ($file in $files) {
    Write-Host "Fixing $($file.Name)"
    $content = Get-Content $file.FullName -Raw
    foreach ($old in $fixes.Keys) {
        $new = $fixes[$old]
        $content = $content.Replace($old, $new) # Case-sensitive replace
    }
    Set-Content -Path $file.FullName -Value $content -NoNewline
}
