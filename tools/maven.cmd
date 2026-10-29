@echo off
setlocal
rem Prefer the configured JDK; otherwise use a locally installed Oracle/OpenJDK folder.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto findMaven
for /d %%J in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~J\bin\javac.exe" set "JAVA_HOME=%%~J"
:findMaven
where mvn.cmd >nul 2>nul
if not errorlevel 1 (
  goto pathMaven
)
rem IntelliJ/Maven wrapper distributions are often already cached on student PCs.
set "CACHED_MAVEN="
for /r "%USERPROFILE%\.m2\wrapper\dists" %%M in (mvn.cmd) do if exist "%%M" set "CACHED_MAVEN=%%M"
if defined CACHED_MAVEN (
  goto cachedMaven
)
echo Maven was not found. Install Maven and add its bin folder to PATH, or run Maven from IntelliJ IDEA.
exit /b 1

:pathMaven
call mvn.cmd %*
exit /b %errorlevel%
:cachedMaven
call "%CACHED_MAVEN%" %*
exit /b %errorlevel%
