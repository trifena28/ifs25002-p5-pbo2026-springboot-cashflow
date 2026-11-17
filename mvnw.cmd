@echo off
setlocal EnableDelayedExpansion

set "BASE_DIR=%~dp0"
set "PROPS_FILE=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"

for /f "usebackq tokens=1,* delims==" %%a in ("%PROPS_FILE%") do (
    if "%%a"=="distributionUrl" set "DIST_URL=%%b"
)

for %%f in ("%DIST_URL%") do set "ZIP_NAME=%%~nxf"
set "MVN_DIR_NAME=%ZIP_NAME:-bin.zip=%"

set "WRAPPER_DISTS=%USERPROFILE%\.m2\wrapper\dists\%MVN_DIR_NAME%"
set "MVN_BIN=%WRAPPER_DISTS%\%MVN_DIR_NAME%\bin\mvn.cmd"
set "TMP_ZIP=%WRAPPER_DISTS%\%ZIP_NAME%"

if not exist "%MVN_BIN%" (
    if not exist "%WRAPPER_DISTS%" mkdir "%WRAPPER_DISTS%"
    echo Downloading Maven from %DIST_URL% ...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%TMP_ZIP%'; Expand-Archive -Path '%TMP_ZIP%' -DestinationPath '%WRAPPER_DISTS%' -Force; Remove-Item '%TMP_ZIP%' -Force"
)

call "%MVN_BIN%" %*