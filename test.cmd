@echo off
setlocal
cd /d "%~dp0"
call tools\maven.cmd clean test
if errorlevel 1 (
  echo Tests failed. Read the output above and target\surefire-reports.
  pause
  exit /b 1
)
echo All tests passed.
pause
