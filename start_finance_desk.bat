@echo off
set PROJECT_ROOT=E:\Repos\finance-engineering-desk
set FRONTEND_DIR=%PROJECT_ROOT%\frontend
set BACKEND_PORT=8080
set FRONTEND_PORT=3000

echo Starting Finance Backend in background...
cd /d %PROJECT_ROOT%
start /b cmd /c "mvn spring-boot:run" > nul 2>&1

echo Starting Finance Frontend in background...
cd /d %FRONTEND_DIR%
start /b cmd /c "npm run dev" > nul 2>&1

echo Waiting for servers to start (15 seconds)...
timeout /t 15 /nobreak >nul

echo Capturing PIDs...
set "BACKEND_PID="
for /f "tokens=2" %%i in ('wmic process where "name='java.exe' and commandline like '%%FinanceTrackerApplication%%'" get processid /value ^| find "ProcessId"') do set "BACKEND_PID=%%i"
if defined BACKEND_PID (
    echo Backend PID: %BACKEND_PID%
) else (
    echo Failed to get Backend PID. Ensure Maven is in PATH and the command is correct.
)

set "FRONTEND_PID="
for /f "tokens=2" %%i in ('wmic process where "name='node.exe' and commandline like '%%vite%%'" get processid /value ^| find "ProcessId"') do set "FRONTEND_PID=%%i"
if defined FRONTEND_PID (
    echo Frontend PID: %FRONTEND_PID%
) else (
    echo Failed to get Frontend PID.
)

echo Opening Chrome to frontend dashboard (http://localhost:%FRONTEND_PORT%)...
start chrome http://localhost:%FRONTEND_PORT%

echo.
echo =================================================================================
echo Servers are running in the background.
echo To stop both backend and frontend, simply close *this* command window.
echo =================================================================================
pause >nul

echo Stopping servers...
if defined BACKEND_PID (
    taskkill /pid %BACKEND_PID% /f >nul 2>&1
    echo Backend (PID %BACKEND_PID%) stopped.
) else (
    echo Backend PID not found, could not stop.
)
if defined FRONTEND_PID (
    taskkill /pid %FRONTEND_PID% /f >nul 2>&1
    echo Frontend (PID %FRONTEND_PID%) stopped.
) else (
    echo Frontend PID not found, could not stop.
)
echo All processes terminated.
exit
