# 📱 Inspiration Notes - Build Instructions

## 🚀 Quick Start (Recommended)

如果Android Studio有问题，使用这些脚本可以直接构建APK：

### 方法1：一键快速构建
```bash
# 双击运行
quick_build.bat
```
- ✅ 最简单的方法
- ✅ 自动拉取最新代码
- ✅ 直接构建APK
- ⏱️ 大约3-5分钟

### 方法2：完整构建（推荐）
```bash
# 双击运行
build_app.bat
```
- ✅ 完整的环境检查
- ✅ 详细的构建过程
- ✅ 自动打开APK文件夹
- ✅ 中文界面友好

### 方法3：PowerShell版本（高级）
```powershell
# 右键 → "用PowerShell运行"
.\build_app.ps1

# 或者带参数
.\build_app.ps1 -Clean    # 清理构建
.\build_app.ps1 -Release  # 发布版本
.\build_app.ps1 -Help     # 查看帮助
```
- ✅ 最强大的功能
- ✅ 彩色输出
- ✅ 详细的构建信息
- ✅ 支持多种构建选项

### 方法4：故障排除版本
```bash
# 如果其他方法都失败了
fix_and_build.bat
```
- 🔧 自动修复常见问题
- 🔧 多次构建尝试
- 🔧 详细的错误诊断
- 🔧 手动故障排除指导

## 📋 使用步骤

### 1. 准备工作
1. 确保电脑已安装 **Java 17+**
   - 下载地址：https://adoptium.net/
2. 确保已安装 **Git**
   - 下载地址：https://git-scm.com/
3. 下载项目代码到本地

### 2. 运行构建脚本
1. 打开项目文件夹
2. 双击运行任一构建脚本
3. 等待构建完成（3-10分钟）
4. APK文件会自动打开所在文件夹

### 3. 安装APK
1. 将 `app-debug.apk` 复制到Android设备
2. 在Android设备上：设置 → 安全 → 启用"未知来源"
3. 点击APK文件安装
4. 应用名称显示为 "Inspiration Notes"

## 🔧 常见问题解决

### 问题1：Java版本错误
```
错误：This tool requires JDK 17 or later
解决：安装Java 17+ from https://adoptium.net/
```

### 问题2：网络连接问题
```
错误：Could not resolve dependencies
解决：检查网络连接，或使用VPN
```

### 问题3：权限问题
```
错误：Access denied
解决：以管理员身份运行脚本
```

### 问题4：Gradle问题
```
错误：Gradle build failed
解决：运行 fix_and_build.bat 进行自动修复
```

## 📁 输出文件位置

构建成功后，APK文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

## 🎯 给英文老师展示

构建完成后，你可以：

1. **安装到手机**：展示完整的移动应用
2. **展示代码**：GitHub仓库 https://github.com/licctvcctv/Action
3. **说明功能**：
   - ✅ 完全英文界面
   - ✅ 创建和编辑笔记
   - ✅ 分类管理
   - ✅ 搜索功能
   - ✅ 云端同步
   - ✅ Material Design界面

## 🆘 如果所有方法都失败

1. **使用GitHub Actions构建的APK**：
   - 访问：https://github.com/licctvcctv/Action/actions
   - 下载最新的构建产物

2. **联系技术支持**：
   - 提供错误日志
   - 说明系统环境（Windows版本、Java版本等）

## 📞 技术规格

- **开发语言**：Java
- **框架**：Android Native
- **最低Android版本**：API 21 (Android 5.0)
- **目标Android版本**：API 33 (Android 13)
- **构建工具**：Gradle 8.5
- **界面设计**：Material Design 3
- **数据库**：Supabase (PostgreSQL)

---

🎉 **祝你向英文老师展示成功！**
