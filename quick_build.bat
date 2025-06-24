@echo off
title Inspiration Notes - Quick Build
color 0A

echo.
echo ╔══════════════════════════════════════╗
echo ║     Inspiration Notes Quick Build    ║
echo ╚══════════════════════════════════════╝
echo.

:: 快速检查和构建
echo 🔄 Starting quick build...
echo.

:: 拉取最新代码
echo 📥 Updating code...
git pull origin main >nul 2>&1

:: 直接构建
echo 🔨 Building APK (this may take 3-5 minutes)...
echo.

gradlew.bat assembleDebug

:: 检查结果
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ✅ SUCCESS! APK built successfully!
    echo.
    echo 📱 APK ready at: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 🚀 Opening folder...
    start "" "app\build\outputs\apk\debug"
    echo.
    echo 📋 Next steps:
    echo    1. Copy app-debug.apk to your Android device
    echo    2. Enable "Install from Unknown Sources" in Android settings
    echo    3. Install the APK
    echo.
) else (
    echo.
    echo ❌ Build failed or APK not found
    echo.
    echo 🔧 Try these solutions:
    echo    1. Run: gradlew clean assembleDebug
    echo    2. Check internet connection
    echo    3. Ensure Java 17+ is installed
    echo.
)

echo Press any key to exit...
pause >nul
