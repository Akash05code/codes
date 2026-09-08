@echo off

cd /d "%~dp0"

if not exist "logs" mkdir "logs"

set "LOGFILE=%~dp0logs\automation-%date:~10,4%-%date:~4,2%-%date:~7,2%.log"

echo ========================================== >> "%LOGFILE%"
echo Automation Started: %date% %time% >> "%LOGFILE%"
echo Project Directory: %CD% >> "%LOGFILE%"
echo ========================================== >> "%LOGFILE%"

echo. >> "%LOGFILE%"
echo [1] Java Version >> "%LOGFILE%"
java -version >> "%LOGFILE%" 2>&1

echo. >> "%LOGFILE%"
echo [2] Maven Version >> "%LOGFILE%"
call mvn -version >> "%LOGFILE%" 2>&1

echo. >> "%LOGFILE%"
echo [3] Maven Clean Compile >> "%LOGFILE%"

call mvn clean compile >> "%LOGFILE%" 2>&1

set "COMPILE_EXIT=%ERRORLEVEL%"

echo Compile Exit Code: %COMPILE_EXIT% >> "%LOGFILE%"

if not "%COMPILE_EXIT%"=="0" (
    echo. >> "%LOGFILE%"
    echo BUILD FAILED - AUTOMATION NOT STARTED >> "%LOGFILE%"
    echo Finished: %date% %time% >> "%LOGFILE%"
    exit /b %COMPILE_EXIT%
)

echo. >> "%LOGFILE%"
echo [4] Starting Selenium Automation >> "%LOGFILE%"

call mvn exec:java >> "%LOGFILE%" 2>&1

set "AUTOMATION_EXIT=%ERRORLEVEL%"

echo. >> "%LOGFILE%"
echo Automation Exit Code: %AUTOMATION_EXIT% >> "%LOGFILE%"
echo Automation Finished: %date% %time% >> "%LOGFILE%"
echo ========================================== >> "%LOGFILE%"

exit /b %AUTOMATION_EXIT%