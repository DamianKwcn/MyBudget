$services = @(
    "common",
    "accounts",
    "configserver",
    "eurekaserver",
    "gateway",
    "orchestrator",
    "transactions"
)

$root = "."

foreach ($service in $services) {
    $servicePath = Join-Path $root $service
    if (Test-Path $servicePath) {
        Write-Host "-----------------------------------------"
        Write-Host "Building $service..."
        Push-Location $servicePath
        mvn clean install -DskipTests
        Pop-Location
        Write-Host "$service built successfully!"
    } else {
        Write-Host "-----------------------------------------"
        Write-Host "SKIPPING $service - folder $servicePath not found!"
    }
}

Write-Host "-----------------------------------------"
Write-Host "All builds finished!"
