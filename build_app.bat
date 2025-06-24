@echo off
chcp 65001 >nul
echo ========================================
echo    Inspiration Notes Auto Build Script
echo ========================================
echo.

:: 检查是否安装了Git
echo [1/6] Checking Git installation...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Git is not installed or not in PATH
    echo Please install Git from: https://git-scm.com/download/win
    pause
    exit /b 1
)
echo ✅ Git is available

:: 检查是否在项目目录
echo.
echo [2/6] Checking project directory...
if not exist "app\build.gradle" (
    echo ❌ Not in the correct project directory
    echo Please run this script from the project root directory
    pause
    exit /b 1
)
echo ✅ Project directory confirmed

:: 拉取最新代码
echo.
echo [3/6] Pulling latest code from GitHub...
git pull origin main
if %errorlevel% neq 0 (
    echo ⚠️ Git pull failed, continuing with local code...
)

:: 清理之前的构建
echo.
echo [4/6] Cleaning previous builds...
if exist "app\build\outputs\apk" (
    rmdir /s /q "app\build\outputs\apk"
    echo ✅ Cleaned previous APK files
)

:: 检查Gradle Wrapper
echo.
echo [5/6] Checking Gradle Wrapper...
if not exist "gradlew.bat" (
    echo ❌ Gradle Wrapper not found
    echo Please ensure gradlew.bat exists in the project root
    pause
    exit /b 1
)
echo ✅ Gradle Wrapper found

:: 构建APK
echo.
echo [6/6] Building APK...
echo This may take several minutes, please wait...
echo.

call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo.
    echo ❌ Build failed!
    echo.
    echo Common solutions:
    echo 1. Check internet connection for dependency downloads
    echo 2. Ensure Java 17+ is installed
    echo 3. Try running: gradlew clean assembleDebug
    echo 4. Check Android SDK installation
    echo.
    pause
    exit /b 1
)

:: 检查APK是否生成
echo.
echo Checking build output...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ✅ BUILD SUCCESSFUL!
    echo.
    echo 📱 APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo 📁 Opening output folder...
    explorer "app\build\outputs\apk\debug"
    echo.
    echo You can now install the APK on your Android device!
) else (
    echo ❌ APK file not found after build
    echo Please check the build output above for errors
)

echo.
echo ========================================
echo           Build Process Complete
echo ========================================
pause
