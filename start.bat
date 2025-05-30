@echo off
echo 启动卡密发货系统...
echo.

echo 1. 启动后端服务...
cd service
start "后端服务" cmd /k "mvnw.cmd spring-boot:run"
cd ..

echo 2. 等待后端服务启动...
timeout /t 10 /nobreak

echo 3. 启动前端服务...
cd ui
start "前端服务" cmd /k "npm run dev"
cd ..

echo.
echo 系统启动完成！
echo 后端服务: http://localhost:8080
echo 前端服务: http://localhost:5173
echo 数据库控制台: http://localhost:8080/h2-console
echo.
pause 