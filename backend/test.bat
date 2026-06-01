@echo off
REM =============================================================
REM 供应商协同系统 - 自动化测试执行脚本
REM =============================================================
REM 用法:
REM   test.bat             → 执行所有测试
REM   test.bat unit        → 仅执行单元测试
REM   test.bat api         → 仅执行 API 契约测试
REM   test.bat security    → 仅执行安全隔离测试
REM   test.bat smoke       → 仅执行冒烟测试
REM   test.bat arch        → 仅执行架构测试
REM   test.bat path        → 仅执行前后端路径校验
REM =============================================================

setlocal enabledelayedexpansion
cd /d "%~dp0..\.."

set TEST_REPORT_DIR=target\test-reports
set TEST_RESULT=0

echo ============================================================
echo   供应商协同系统 - 自动化测试
echo ============================================================
echo.

if not exist "%TEST_REPORT_DIR%" mkdir "%TEST_REPORT_DIR%"

if "%~1"=="" goto :run_all
if "%~1"=="unit" goto :run_unit
if "%~1"=="api" goto :run_api
if "%~1"=="security" goto :run_security
if "%~1"=="smoke" goto :run_smoke
if "%~1"=="arch" goto :run_arch
if "%~1"=="path" goto :run_path
echo 未知参数: %~1
echo 用法: test.bat [unit^|api^|security^|smoke^|arch^|path]
exit /b 1

:run_all
echo [1/6] 执行架构规范测试...
call mvn test -pl . -Dtest="com.supplier.test.architecture.*" -DfailIfNoTests=false -q
if errorlevel 1 set TEST_RESULT=1
echo.

echo [2/6] 执行前后端路径一致性校验（静态分析）...
call java -cp "target\test-classes;target\classes" com.supplier.test.scripts.ApiPathValidator
if errorlevel 1 set TEST_RESULT=1
echo.

echo [3/6] 执行 API 契约测试...
call mvn test -pl . -Dtest="com.supplier.test.contract.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
echo.

echo [4/6] 执行供应商隔离安全测试...
call mvn test -pl . -Dtest="com.supplier.test.security.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
echo.

echo [5/6] 执行核心业务冒烟测试...
call mvn test -pl . -Dtest="com.supplier.test.smoke.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
echo.

echo [6/6] 执行所有单元测试...
call mvn test -pl . -Dtest="com.supplier.test.unit.*,com.supplier.**.*Test" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
echo.

goto :report

:run_unit
echo 执行单元测试...
call mvn test -pl . -Dtest="com.supplier.test.unit.*,com.supplier.**.*Test" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
goto :report

:run_api
echo 执行 API 契约测试...
call mvn test -pl . -Dtest="com.supplier.test.contract.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
goto :report

:run_security
echo 执行安全隔离测试...
call mvn test -pl . -Dtest="com.supplier.test.security.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
goto :report

:run_smoke
echo 执行冒烟测试...
call mvn test -pl . -Dtest="com.supplier.test.smoke.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
goto :report

:run_arch
echo 执行架构规范测试...
call mvn test -pl . -Dtest="com.supplier.test.architecture.*" -DfailIfNoTests=false
if errorlevel 1 set TEST_RESULT=1
goto :report

:run_path
echo 执行前后端路径一致性校验...
call java -cp "target\test-classes;target\classes" com.supplier.test.scripts.ApiPathValidator
if errorlevel 1 set TEST_RESULT=1
goto :report

:report
echo.
echo ============================================================
if %TEST_RESULT%==0 (
    echo   [PASS]  所有测试通过！
) else (
    echo   [FAIL]  存在测试失败，请查看上方输出。
)
echo ============================================================
endlocal
exit /b %TEST_RESULT%