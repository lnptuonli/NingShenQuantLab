@echo off
REM ========================================
REM GitHub 推送脚本
REM ========================================

echo.
echo ========================================
echo   推送项目到 GitHub
echo ========================================
echo.

REM 检查是否已设置远程仓库
git remote -v | findstr "origin" >nul
if %errorlevel% neq 0 (
    echo [步骤 1] 设置远程仓库地址
    echo.
    set /p GITHUB_USERNAME="请输入你的 GitHub 用户名: "
    git remote add origin https://github.com/!GITHUB_USERNAME!/alphaForge_demo1.git
    echo 远程仓库已设置！
    echo.
) else (
    echo [步骤 1] 远程仓库已存在
    git remote -v
    echo.
)

echo [步骤 2] 重命名分支为 main
git branch -M main
echo.

echo [步骤 3] 推送到 GitHub
echo 正在推送...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   推送成功！ 🎉
    echo ========================================
    echo.
    echo 访问你的项目：
    echo https://github.com/你的用户名/alphaForge_demo1
    echo.
) else (
    echo.
    echo ========================================
    echo   推送失败！ ❌
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. GitHub 仓库未创建
    echo 2. 认证失败（需要使用 Personal Access Token）
    echo 3. 网络问题
    echo.
    echo 请查看 GITHUB_SETUP.md 获取详细帮助
    echo.
)

pause


