# =============================================================
# 供应商协同系统 - 自动化测试执行脚本 (PowerShell)
# =============================================================
# 用法:
#   .\test.ps1            → 执行所有测试
#   .\test.ps1 -Type unit → 仅执行单元测试
#   .\test.ps1 -Type api  → 仅执行 API 契约测试
#   .\test.ps1 -Type security → 仅执行安全隔离测试
#   .\test.ps1 -Type smoke   → 仅执行冒烟测试
#   .\test.ps1 -Type arch    → 仅执行架构测试
#   .\test.ps1 -Type path    → 仅执行前后端路径校验
# =============================================================

param(
    [ValidateSet("all", "unit", "api", "security", "smoke", "arch", "path")]
    [string]$Type = "all"
)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent $PSScriptRoot
$TestResult = 0

Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "  供应商协同系统 - 自动化测试" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host ""

function Invoke-MavenTest {
    param([string]$TestClass, [string]$DisplayName)

    Write-Host "[$DisplayName]" -ForegroundColor Yellow
    $testArgs = @("test", "-pl", ".", "-Dtest=$TestClass", "-DfailIfNoTests=false", "--no-transfer-progress")

    $process = Start-Process -FilePath "mvn" -ArgumentList $testArgs -NoNewWindow -PassThru -Wait
    Write-Host ""

    if ($process.ExitCode -ne 0) {
        $script:TestResult = 1
        Write-Host "  ✗ 失败 (exit code: $($process.ExitCode))" -ForegroundColor Red
    } else {
        Write-Host "  ✓ 通过" -ForegroundColor Green
    }
}

function Invoke-PathValidator {
    Write-Host "[前后端 API 路径一致性校验]" -ForegroundColor Yellow
    $javaArgs = @(
        "-cp", "target\test-classes;target\classes",
        "com.supplier.test.scripts.ApiPathValidator"
    )
    $process = Start-Process -FilePath "java" -ArgumentList $javaArgs -NoNewWindow -PassThru -Wait
    Write-Host ""
    if ($process.ExitCode -ne 0) {
        $script:TestResult = 1
        Write-Host "  ✗ 前后端路径不一致！" -ForegroundColor Red
    } else {
        Write-Host "  ✓ 通过" -ForegroundColor Green
    }
}

Push-Location $RootDir

try {
    switch ($Type) {
        "all" {
            Invoke-MavenTest -TestClass "com.supplier.test.architecture.*" -DisplayName "1/6 架构规范测试"
            Invoke-PathValidator
            Write-Host "[2/6 前后端路径校验]" -ForegroundColor Yellow
            Invoke-MavenTest -TestClass "com.supplier.test.contract.*" -DisplayName "3/6 API 契约测试"
            Invoke-MavenTest -TestClass "com.supplier.test.security.*" -DisplayName "4/6 安全隔离测试"
            Invoke-MavenTest -TestClass "com.supplier.test.smoke.*" -DisplayName "5/6 核心业务冒烟测试"
            Invoke-MavenTest -TestClass "com.supplier.test.unit.*,com.supplier.**.*Test" -DisplayName "6/6 单元测试"
        }
        "unit"  { Invoke-MavenTest -TestClass "com.supplier.test.unit.*,com.supplier.**.*Test" -DisplayName "单元测试" }
        "api"   { Invoke-MavenTest -TestClass "com.supplier.test.contract.*" -DisplayName "API 契约测试" }
        "security" { Invoke-MavenTest -TestClass "com.supplier.test.security.*" -DisplayName "安全隔离测试" }
        "smoke" { Invoke-MavenTest -TestClass "com.supplier.test.smoke.*" -DisplayName "冒烟测试" }
        "arch"  { Invoke-MavenTest -TestClass "com.supplier.test.architecture.*" -DisplayName "架构规范测试" }
        "path"  { Invoke-PathValidator }
    }
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "=" * 60 -ForegroundColor Cyan
if ($TestResult -eq 0) {
    Write-Host "  [PASS]  所有测试通过！" -ForegroundColor Green
} else {
    Write-Host "  [FAIL]  存在测试失败，请查看上方输出。" -ForegroundColor Red
}
Write-Host "=" * 60 -ForegroundColor Cyan

exit $TestResult