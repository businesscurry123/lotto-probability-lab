@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\launch-lotto-preview.ps1" -DeviceProfile GalaxyS25Ultra %*
pause
