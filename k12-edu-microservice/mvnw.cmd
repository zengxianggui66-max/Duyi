@echo off
setlocal
set "MVNW_PROJECTBASEDIR=%~dp0"
set "MVNW_MAVEN_VERSION=3.9.9"
set "MVNW_MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVNW_MAVEN_VERSION%"
set "MVNW_MAVEN_BIN=%MVNW_MAVEN_DIR%\apache-maven-%MVNW_MAVEN_VERSION%\bin\mvn.cmd"
if exist "%MVNW_MAVEN_BIN%" goto run_maven
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $ErrorActionPreference='Stop'; $version='%MVNW_MAVEN_VERSION%'; $dest='%MVNW_MAVEN_DIR%'; $zip=Join-Path $dest ('apache-maven-' + $version + '-bin.zip'); New-Item -ItemType Directory -Force -Path $dest | Out-Null; if (!(Test-Path $zip)) { Invoke-WebRequest -Uri ('https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/' + $version + '/apache-maven-' + $version + '-bin.zip') -OutFile $zip }; Expand-Archive -Path $zip -DestinationPath $dest -Force }"
if errorlevel 1 exit /b %errorlevel%
:run_maven
call "%MVNW_MAVEN_BIN%" %*
endlocal
