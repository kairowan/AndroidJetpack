# ===============================
# Windows PowerShell Android 打包脚本（专为 MVVM 项目）
# 保留 Bash 脚本全部功能
# ===============================

$ErrorActionPreference = "Stop"

function Info($msg)  { Write-Host ">>> $msg" -ForegroundColor Cyan }
function Ok($msg)    { Write-Host "✔ $msg"  -ForegroundColor Green }
function Warn($msg)  { Write-Host "⚠ $msg"  -ForegroundColor Yellow }
function Err($msg)   { Write-Host "✘ $msg"  -ForegroundColor Red }

# -------------------------------
# 项目路径
# -------------------------------
$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ProjectRoot = Resolve-Path "$ScriptDir\.."
Set-Location $ProjectRoot

$OutputDir = "$ProjectRoot\output"
$Gradlew   = "$ProjectRoot\gradlew"
$PropFile  = "$ProjectRoot\gradle.properties"

# -------------------------------
# 选择构建类型
# -------------------------------
Write-Host "请选择打包方式：" -ForegroundColor Cyan
Write-Host "1) APK Release"
Write-Host "2) APK Debug"
Write-Host "3) AAB Release"
Write-Host "4) 退出"

$sel = Read-Host "> 输入数字选择"

switch ($sel) {
    "1" { $PackageType="apk";  $BuildType="Release" }
    "2" { $PackageType="apk";  $BuildType="Debug" }
    "3" { $PackageType="aab";  $BuildType="Release" }
    default { exit }
}

Info "你选择了：$PackageType $BuildType"

# -------------------------------
# 自动探测 APP module
# -------------------------------
Info "正在自动探测应用模块..."

$AppModule = Get-ChildItem -Recurse -Filter "build.gradle*" |
    Select-String "com.android.application" |
    Select-Object -First 1 |
    ForEach-Object { Split-Path $_.Path -Parent } |
    ForEach-Object {
        $_.Replace("$ProjectRoot\", "")
    }

if (!$AppModule) {
    Warn "未找到 application 模块，默认使用 app"
    $AppModule = "app"
}

Ok "应用模块：$AppModule"

# -------------------------------
# 读取签名文件
# -------------------------------
Info "读取签名信息..."

function ReadProp($key) {
    $line = Select-String -Path $PropFile -Pattern "^$key=" | Select-Object -First 1
    if ($line) { return $line.ToString().Split("=")[1].Trim() }
    return $null
}

$KeystorePathRaw = ReadProp "KEYSTORE_PATH"
$Alias           = ReadProp "KEYSTORE_ALIAS"
$StorePass       = ReadProp "KEYSTORE_PASS"
$KeyPass         = ReadProp "KEY_PASS"

# 如果 gradle.properties 没找到 → 从 build.gradle.kts 查找
if (!$KeystorePathRaw -or !$Alias) {
    Info "gradle.properties 未定义签名，从 build.gradle(.kts) 解析..."

    $gradleFile = "$ProjectRoot\$AppModule\build.gradle.kts"
    if (!(Test-Path $gradleFile)) {
        $gradleFile = "$ProjectRoot\$AppModule\build.gradle"
    }

    if (Test-Path $gradleFile) {
        $KeystorePathRaw = Select-String -Path $gradleFile -Pattern "storeFile\s*=\s*file" |
            ForEach-Object { ($_ -replace '.*file\("([^"]+)"\).*', '$1') }

        $Alias = Select-String -Path $gradleFile -Pattern "keyAlias" |
            ForEach-Object { ($_ -replace '.*"([^"]+)".*', '$1') }

        $StorePass = Select-String -Path $gradleFile -Pattern "storePassword" |
            ForEach-Object { ($_ -replace '.*"([^"]+)".*', '$1') }

        $KeyPass = Select-String -Path $gradleFile -Pattern "keyPassword" |
            ForEach-Object { ($_ -replace '.*"([^"]+)".*', '$1') }
    }
}

if (!$KeystorePathRaw) { Err "无法找到签名文件"; exit }

if (Test-Path "$ProjectRoot\$AppModule\$KeystorePathRaw") {
    $KeystorePath = "$ProjectRoot\$AppModule\$KeystorePathRaw"
} else {
    $KeystorePath = $KeystorePathRaw
}

Ok "签名文件：$KeystorePath"

# -------------------------------
# 清理旧构建
# -------------------------------
Info "开始清理..."
& $Gradlew clean

# -------------------------------
# 执行 assemble/bundle
# -------------------------------
$Task = if ($PackageType -eq "apk") {
    "assemble$BuildType"
} else {
    "bundle$BuildType"
}

Info "开始构建：$Task"
& $Gradlew ":$AppModule:$Task" `
    "-PKEYSTORE_PATH=$KeystorePath" `
    "-PKEYSTORE_ALIAS=$Alias" `
    "-PKEYSTORE_PASS=$StorePass" `
    "-PKEY_PASS=$KeyPass"

# -------------------------------
# 收集产物
# -------------------------------
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

if ($PackageType -eq "apk") {
    $files = Get-ChildItem "$ProjectRoot\$AppModule\build\outputs\apk" -Recurse -Filter "*$($BuildType.ToLower()).apk"
} else {
    $files = Get-ChildItem "$ProjectRoot\$AppModule\build\outputs\bundle" -Recurse -Filter "*$($BuildType.ToLower()).aab"
}

foreach ($f in $files) {
    Copy-Item $f.FullName "$OutputDir\"
    Ok "已生成：$($f.Name)"
}

Ok "全部完成！输出目录：$OutputDir"
