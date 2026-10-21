@echo off
chcp 65001 >nul
cd /d D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\backend
echo ============================================
echo   千年诗境后端 启动中...
echo   启动成功后浏览器访问: http://localhost:8080/api/test
echo ============================================
echo.
"C:\Users\LENOVO\.jdks\ms-21.0.10\bin\java.exe" -jar target\backend-1.0.0.jar
pause
