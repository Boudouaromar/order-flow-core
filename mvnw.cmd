@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows (no jar required)
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set MAVEN_WRAPPER_PROPERTIES=.mvn\wrapper\maven-wrapper.properties
set MAVEN_USER_HOME=%USERPROFILE%\.m2
set MAVEN_WRAPPER_CACHE=%MAVEN_USER_HOME%\wrapper

@REM Read distributionUrl
for /f "tokens=2 delims==" %%i in ('findstr /i distributionUrl "%MAVEN_WRAPPER_PROPERTIES%"') do set DISTRIBUTION_URL=%%i
for %%f in ("%DISTRIBUTION_URL%") do set MAVEN_DIST_NAME=%%~nf
set MAVEN_HOME=%MAVEN_WRAPPER_CACHE%\%MAVEN_DIST_NAME%

@REM Download if not cached
if not exist "%MAVEN_HOME%" (
  echo Downloading Maven: %DISTRIBUTION_URL%
  if not exist "%MAVEN_WRAPPER_CACHE%" mkdir "%MAVEN_WRAPPER_CACHE%"
  set TMP_DOWNLOAD=%MAVEN_WRAPPER_CACHE%\tmp-download.zip
  powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '!TMP_DOWNLOAD!'"
  powershell -Command "Expand-Archive -Path '!TMP_DOWNLOAD!' -DestinationPath '%MAVEN_WRAPPER_CACHE%'"
  del "!TMP_DOWNLOAD!"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
