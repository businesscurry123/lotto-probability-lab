param(
    [string]$AvdName = "Medium_Phone_API_30",
    [int]$AdbPort = 5037,
    [ValidateSet("Default", "GalaxyS25Ultra")]
    [string]$DeviceProfile = "Default"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$sdkRoot = if ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} else {
    Join-Path $env:LOCALAPPDATA "Android\Sdk"
}
$gradleWrapper = Join-Path $repoRoot "gradlew.bat"
$adb = Join-Path $sdkRoot "platform-tools\adb.exe"
$emulator = Join-Path $sdkRoot "emulator\emulator.exe"
$apk = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
$packageName = "com.lottolab.probability"
$activity = "$packageName/.MainActivity"

function Assert-Exists([string]$Path, [string]$Label) {
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "$Label not found: $Path"
    }
}

function Get-AdbDevices {
    $output = & $adb -P $AdbPort devices 2>$null
    if ($LASTEXITCODE -ne 0) {
        return @()
    }

    return @(
        $output |
            Select-String -Pattern "^\S+\s+device$" |
            ForEach-Object { ($_ -split "\s+")[0] }
    )
}

function Invoke-Adb([string[]]$Arguments) {
    & $adb -P $AdbPort @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "ADB command failed: adb $($Arguments -join ' ')"
    }
}

function Get-PrimaryDevice {
    $devices = @(Get-AdbDevices)
    if ($devices.Count -eq 0) {
        return $null
    }

    return $devices[0]
}

function Wait-ForAndroidBoot {
    for ($attempt = 0; $attempt -lt 72; $attempt++) {
        $devices = @(Get-AdbDevices)
        if ($devices.Count -gt 0) {
            $bootCompleted = (& $adb -P $AdbPort shell getprop sys.boot_completed 2>$null).Trim()
            if ($bootCompleted -eq "1") {
                return
            }
        }
        Start-Sleep -Seconds 5
    }

    throw "Android emulator did not finish booting in time."
}

function Set-PreviewDeviceProfile([string]$Device) {
    if ($DeviceProfile -ne "GalaxyS25Ultra") {
        return
    }

    if ($Device -notlike "emulator-*") {
        Write-Host "Galaxy S25 Ultra display profile is only applied to emulators. Skipping display override for $Device." -ForegroundColor Yellow
        return
    }

    Write-Host "Applying Galaxy S25 Ultra preview size: 1440x3120, density 560..." -ForegroundColor Cyan
    Invoke-Adb @("-s", $Device, "shell", "wm", "size", "1440x3120")
    Invoke-Adb @("-s", $Device, "shell", "wm", "density", "560")
}

Assert-Exists $gradleWrapper "Gradle wrapper"
Assert-Exists $adb "ADB"
Assert-Exists $emulator "Android emulator"

$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_ADB_SERVER_PORT = "$AdbPort"

Write-Host ""
Write-Host "Building latest Lotto preview..." -ForegroundColor Cyan
Push-Location $repoRoot
try {
    & $gradleWrapper :app:assembleDebug --console=plain
    if ($LASTEXITCODE -ne 0) {
        throw "Debug APK build failed."
    }
} finally {
    Pop-Location
}
Assert-Exists $apk "Debug APK"

Write-Host "Starting ADB on port $AdbPort..." -ForegroundColor Cyan
& $adb -P $AdbPort start-server
if ($LASTEXITCODE -ne 0) {
    throw "ADB server could not start on port $AdbPort."
}

if (@(Get-AdbDevices).Count -eq 0) {
    $avds = & $emulator -list-avds
    if ($AvdName -notin $avds) {
        throw "Android virtual device not found: $AvdName"
    }

    Write-Host "Opening Android emulator $AvdName..." -ForegroundColor Cyan
    Start-Process `
        -FilePath $emulator `
        -ArgumentList "-avd", $AvdName, "-no-snapshot-load"
}

Write-Host "Waiting for Android to be ready..." -ForegroundColor Cyan
Wait-ForAndroidBoot
$targetDevice = Get-PrimaryDevice
if (-not $targetDevice) {
    throw "No Android device is available after boot."
}
Set-PreviewDeviceProfile $targetDevice

Write-Host "Installing latest preview..." -ForegroundColor Cyan
Invoke-Adb @("-s", $targetDevice, "install", "-r", $apk)

Invoke-Adb @("-s", $targetDevice, "shell", "am", "force-stop", $packageName)
Invoke-Adb @("-s", $targetDevice, "shell", "am", "start", "-n", $activity)

Write-Host ""
Write-Host "Lotto preview is open on $targetDevice." -ForegroundColor Green
