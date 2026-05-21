@echo off
SETLOCAL EnableDelayedExpansion

echo ============================================================
echo   FINANCE ENGINEERING DESK - DEPLOYMENT TERMINAL
echo ============================================================
echo.

:: Check for JDK 17
java -version 2>&1 | findstr /i "version 17." > nul
if %errorlevel% neq 0 (
    echo [ERROR] JDK 17 is required. Please install it and add to PATH.
    pause
    exit /b 1
)

:: Check for Node.js
node -v > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is required. Please install it and add to PATH.
    pause
    exit /b 1
)

echo [1/3] Building Backend Node...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed.
    pause
    exit /b 1
)

echo.
echo [2/3] Preparing Frontend Console...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Frontend installation failed.
    pause
    exit /b 1
)

echo.
echo [3/3] Launching Integrated Environment...
echo.
echo Dashboard will be available at: http://localhost:3000
echo API Node running at: http://localhost:8080
echo.

cd ..
:: Start Backend in background
start "FED-BACKEND" /min mvn spring-boot:run

cd frontend
:: Start Frontend
call npm run dev

pause