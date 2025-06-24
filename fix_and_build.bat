@echo off
chcp 65001 >nul
title Inspiration Notes - Fix & Build
color 0B

echo.
echo ╔════════════════════════════════════════════╗
echo ║     Inspiration Notes Fix & Build Tool    ║
echo ╚════════════════════════════════════════════╝
echo.

echo 🔧 This script will try to fix common Android Studio issues
echo    and build the APK using command line tools.
echo.

:: 1. 环境检查
echo [Step 1] Environment Check
echo ═══════════════════════════
echo.

:: 检查Java
echo 🔍 Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found
    echo 💡 Please install Java 17+ from: https://adoptium.net/
    echo.
) else (
    echo ✅ Java is available
)

:: 检查Git
echo 🔍 Checking Git...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Git not found
    echo 💡 Please install Git from: https://git-scm.com/
    echo.
) else (
    echo ✅ Git is available
)

:: 检查项目
echo 🔍 Checking project structure...
if not exist "app\build.gradle" (
    echo ❌ Not in Android project directory
    echo 💡 Please run this script from the project root
    pause
    exit /b 1
) else (
    echo ✅ Android project detected
)

echo.

:: 2. 清理和修复
echo [Step 2] Clean & Fix
echo ═══════════════════
echo.

echo 🧹 Cleaning previous builds...
if exist ".gradle" rmdir /s /q ".gradle" >nul 2>&1
if exist "app\build" rmdir /s /q "app\build" >nul 2>&1
if exist "build" rmdir /s /q "build" >nul 2>&1
echo ✅ Cleaned build directories

echo 📥 Updating from GitHub...
git pull origin main >nul 2>&1
echo ✅ Code updated

echo 🔄 Refreshing Gradle wrapper...
if exist "gradlew.bat" (
    echo ✅ Gradle wrapper found
) else (
    echo ❌ Gradle wrapper missing
    echo 💡 Please ensure gradlew.bat exists
    pause
    exit /b 1
)

echo.

:: 3. 构建尝试
echo [Step 3] Build Attempts
echo ═══════════════════════
echo.

echo 🔨 Attempt 1: Clean build...
gradlew.bat clean assembleDebug --stacktrace
if %errorlevel% equ 0 goto :build_success

echo.
echo ⚠️ First attempt failed, trying alternative approaches...
echo.

echo 🔨 Attempt 2: Offline build...
gradlew.bat assembleDebug --offline
if %errorlevel% equ 0 goto :build_success

echo.
echo 🔨 Attempt 3: Force refresh dependencies...
gradlew.bat assembleDebug --refresh-dependencies
if %errorlevel% equ 0 goto :build_success

echo.
echo 🔨 Attempt 4: Build with info logging...
gradlew.bat assembleDebug --info
if %errorlevel% equ 0 goto :build_success

echo.
echo ❌ All build attempts failed
echo.
echo 🆘 Manual troubleshooting needed:
echo    1. Check Android SDK installation
echo    2. Verify ANDROID_HOME environment variable
echo    3. Ensure stable internet connection
echo    4. Try opening project in Android Studio
echo    5. Check system requirements (Java 17+, sufficient disk space)
echo.
goto :end

:build_success
echo.
echo ✅ BUILD SUCCESSFUL!
echo.

:: 检查APK
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo 📱 APK Location: app\build\outputs\apk\debug\app-debug.apk
    
    :: 获取APK大小
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
        set size=%%~zA
        set /a sizeMB=!size!/1024/1024
    )
    
    echo 📏 APK Size: ~%sizeMB% MB
    echo.
    echo 🚀 Opening APK folder...
    start "" "app\build\outputs\apk\debug"
    echo.
    echo 📋 Installation Instructions:
    echo    1. Copy app-debug.apk to your Android device
    echo    2. On Android: Settings → Security → Enable "Unknown Sources"
    echo    3. Open the APK file on your device to install
    echo    4. The app will appear as "Inspiration Notes"
    echo.
    echo 🎉 Ready to show your English teacher!
) else (
    echo ❌ APK file not found despite successful build
    echo 💡 Check the build output directory manually
)

:end
echo.
echo ════════════════════════════════════════════
echo            Process Complete
echo ════════════════════════════════════════════
echo.
pause
