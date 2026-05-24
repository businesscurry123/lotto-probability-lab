param(
    [int]$AdbPort = 5037,
    [string]$DeviceSerial = ""
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

Assert-Exists $gradleWrapper "Gradle wrapper"
Assert-Exists $adb "ADB"

$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_ADB_SERVER_PORT = "$AdbPort"

Write-Host ""
Write-Host "Building latest Lotto debug APK..." -ForegroundColor Cyan
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
Invoke-Adb @("start-server")

$devices = @(Get-AdbDevices)
$physicalDevices = @($devices | Where-Object { $_ -notlike "emulator-*" })
if ($DeviceSerial) {
    if ($DeviceSerial -notin $devices) {
        throw "Requested device is not connected: $DeviceSerial"
    }
    $targetDevice = $DeviceSerial
} elseif ($physicalDevices.Count -eq 1) {
    $targetDevice = $physicalDevices[0]
} elseif ($physicalDevices.Count -gt 1) {
    throw "Multiple phones are connected. Re-run with -DeviceSerial <serial>. Connected phones: $($physicalDevices -join ', ')"
} else {
    throw @"
No physical Android phone is connected.

To install on your Galaxy phone:
1. On the phone, open Settings > About phone > Software information.
2. Tap Build number 7 times to enable Developer options.
3. Open Developer options and turn on USB debugging.
4. Connect the phone with USB and tap Allow on the phone.
5. Run this script again.
"@
}

$model = (& $adb -P $AdbPort -s $targetDevice shell getprop ro.product.model 2>$null).Trim()
$size = (& $adb -P $AdbPort -s $targetDevice shell wm size 2>$null).Trim()
$density = (& $adb -P $AdbPort -s $targetDevice shell wm density 2>$null).Trim()

Write-Host "Installing on $targetDevice $model..." -ForegroundColor Cyan
Write-Host "Device display: $size / $density" -ForegroundColor DarkGray
Invoke-Adb @("-s", $targetDevice, "install", "-r", $apk)
Invoke-Adb @("-s", $targetDevice, "shell", "am", "force-stop", $packageName)
Invoke-Adb @("-s", $targetDevice, "shell", "am", "start", "-n", $activity)

Write-Host ""
Write-Host "Lotto app is installed and open on your phone." -ForegroundColor Green
