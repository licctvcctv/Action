# Inspiration Notes Auto Build Script
# PowerShell版本 - 更强大的错误处理和日志

param(
    [switch]$Clean,
    [switch]$Release,
    [switch]$Help
)

# 颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success { Write-ColorOutput Green $args }
function Write-Error { Write-ColorOutput Red $args }
function Write-Warning { Write-ColorOutput Yellow $args }
function Write-Info { Write-ColorOutput Cyan $args }

# 显示帮助信息
if ($Help) {
    Write-Info "Inspiration Notes Build Script"
    Write-Info "Usage: .\build_app.ps1 [options]"
    Write-Info ""
    Write-Info "Options:"
    Write-Info "  -Clean    Clean build (equivalent to gradlew clean assembleDebug)"
    Write-Info "  -Release  Build release APK instead of debug"
    Write-Info "  -Help     Show this help message"
    Write-Info ""
    Write-Info "Examples:"
    Write-Info "  .\build_app.ps1                # Normal debug build"
    Write-Info "  .\build_app.ps1 -Clean         # Clean debug build"
    Write-Info "  .\build_app.ps1 -Release       # Release build"
    exit 0
}

Write-Info "========================================"
Write-Info "   Inspiration Notes Auto Build Script"
Write-Info "========================================"
Write-Info ""

# 记录开始时间
$startTime = Get-Date

try {
    # 1. 检查Git
    Write-Info "[1/7] Checking Git installation..."
    $gitVersion = git --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Git is not installed or not in PATH"
        Write-Error "Please install Git from: https://git-scm.com/download/win"
        exit 1
    }
    Write-Success "✅ Git is available: $gitVersion"

    # 2. 检查项目目录
    Write-Info ""
    Write-Info "[2/7] Checking project directory..."
    if (!(Test-Path "app\build.gradle")) {
        Write-Error "❌ Not in the correct project directory"
        Write-Error "Please run this script from the project root directory"
        exit 1
    }
    Write-Success "✅ Project directory confirmed"

    # 3. 拉取最新代码
    Write-Info ""
    Write-Info "[3/7] Pulling latest code from GitHub..."
    git pull origin main 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Success "✅ Code updated from GitHub"
    } else {
        Write-Warning "⚠️ Git pull failed, continuing with local code..."
    }

    # 4. 检查Java版本
    Write-Info ""
    Write-Info "[4/7] Checking Java installation..."
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion) {
        Write-Success "✅ Java is available: $javaVersion"
    } else {
        Write-Warning "⚠️ Java version check failed, but continuing..."
    }

    # 5. 清理构建（如果需要）
    Write-Info ""
    Write-Info "[5/7] Preparing build environment..."
    if ($Clean) {
        Write-Info "Performing clean build..."
        if (Test-Path "app\build") {
            Remove-Item -Recurse -Force "app\build" -ErrorAction SilentlyContinue
            Write-Success "✅ Cleaned build directory"
        }
    }

    # 6. 检查Gradle Wrapper
    Write-Info ""
    Write-Info "[6/7] Checking Gradle Wrapper..."
    if (!(Test-Path "gradlew.bat")) {
        Write-Error "❌ Gradle Wrapper not found"
        Write-Error "Please ensure gradlew.bat exists in the project root"
        exit 1
    }
    Write-Success "✅ Gradle Wrapper found"

    # 7. 构建APK
    Write-Info ""
    Write-Info "[7/7] Building APK..."
    
    $buildType = if ($Release) { "assembleRelease" } else { "assembleDebug" }
    $buildCommand = if ($Clean) { "clean $buildType" } else { $buildType }
    
    Write-Info "Build command: gradlew $buildCommand"
    Write-Info "This may take several minutes, please wait..."
    Write-Info ""

    # 执行构建
    $process = Start-Process -FilePath ".\gradlew.bat" -ArgumentList $buildCommand -Wait -PassThru -NoNewWindow
    
    if ($process.ExitCode -ne 0) {
        Write-Error ""
        Write-Error "❌ Build failed!"
        Write-Error ""
        Write-Error "Common solutions:"
        Write-Error "1. Check internet connection for dependency downloads"
        Write-Error "2. Ensure Java 17+ is installed"
        Write-Error "3. Try running with -Clean parameter"
        Write-Error "4. Check Android SDK installation"
        Write-Error "5. Try: Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser"
        exit 1
    }

    # 检查APK文件
    Write-Info ""
    Write-Info "Checking build output..."
    
    $apkPath = if ($Release) { "app\build\outputs\apk\release\app-release-unsigned.apk" } else { "app\build\outputs\apk\debug\app-debug.apk" }
    
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        $buildTime = (Get-Date) - $startTime
        
        Write-Success ""
        Write-Success "✅ BUILD SUCCESSFUL!"
        Write-Success ""
        Write-Success "📱 APK Location: $apkPath"
        Write-Success "📏 APK Size: $([math]::Round($apkSize, 2)) MB"
        Write-Success "⏱️ Build Time: $($buildTime.Minutes)m $($buildTime.Seconds)s"
        Write-Success ""
        Write-Success "📁 Opening output folder..."
        
        # 打开输出文件夹
        $outputDir = Split-Path $apkPath -Parent
        Start-Process explorer $outputDir
        
        Write-Success ""
        Write-Success "You can now install the APK on your Android device!"
        Write-Success "To install: Enable 'Unknown Sources' in Android settings, then install the APK"
        
    } else {
        Write-Error "❌ APK file not found after build"
        Write-Error "Expected location: $apkPath"
        Write-Error "Please check the build output above for errors"
        exit 1
    }

} catch {
    Write-Error ""
    Write-Error "❌ Script execution failed:"
    Write-Error $_.Exception.Message
    exit 1
}

Write-Info ""
Write-Info "========================================"
Write-Info "         Build Process Complete"
Write-Info "========================================"
