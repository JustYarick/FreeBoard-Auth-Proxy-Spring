@echo off
set IMAGE_NAME=yaricks/freeboard-auth-proxy-api
set IMAGE_TAG=1.0.0

echo 🔧 Сборка Maven...
call .\mvnw.cmd clean install
if ERRORLEVEL 1 (
    echo ❌ Maven сборка провалена
    exit /b 1
)

echo 🐳 Сборка Docker образа...
docker build -t %IMAGE_NAME%:%IMAGE_TAG% .
if ERRORLEVEL 1 (
    echo ❌ Docker сборка провалена
    exit /b 1
)

echo ✅ Готово! Образ: %IMAGE_NAME%:%IMAGE_TAG%
pause
