@echo off
cd /d "%~dp0..\app\build\outputs\apk\release\"
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do set IP=%%a
echo Serving APK at:
echo   http://%IP: =%:8080/
echo   Local: http://localhost:8080/
python -m http.server 8080
