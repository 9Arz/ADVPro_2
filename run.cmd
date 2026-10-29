@echo off
setlocal
cd /d "%~dp0"
call tools\maven.cmd javafx:run
if errorlevel 1 (
  echo Application failed. Check Maven, JDK 24 or newer, and your Internet connection for missing dependencies.
  pause
  exit /b 1
)
