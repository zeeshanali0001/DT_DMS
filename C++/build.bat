@echo off
rem Builds the DT-DMS framework with Visual Studio (2019 or later, or the Build Tools) and runs the example.
rem Usage: build.bat            build in Release and run build\bin\DT_DMS_Architecture.exe
rem        build.bat doc        also generate the Doxygen/Graphviz documentation in docs\html
setlocal
cd /d "%~dp0"

rem Already in a Visual Studio developer prompt? Otherwise find Visual Studio with vswhere.
where cl >nul 2>nul
if not errorlevel 1 goto configure
set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
if not exist "%VSWHERE%" goto novs
for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do set "VSDIR=%%i"
if not defined VSDIR goto novs
call "%VSDIR%\VC\Auxiliary\Build\vcvars64.bat" >nul 2>nul

:configure
rem Ninja (shipped with Visual Studio) gives build\bin\*.exe directly; otherwise use the default generator.
set "GENERATOR="
where ninja >nul 2>nul
if not errorlevel 1 set "GENERATOR=-G Ninja"
cmake -S . -B build %GENERATOR% -DCMAKE_BUILD_TYPE=Release
if errorlevel 1 exit /b 1
cmake --build build --config Release
if errorlevel 1 exit /b 1
if /i "%~1"=="doc" cmake --build build --target doc

echo.
echo Running build\bin\DT_DMS_Architecture.exe
echo.
build\bin\DT_DMS_Architecture.exe
exit /b %errorlevel%

:novs
echo Visual Studio with the C++ tools was not found.
echo Install the "Desktop development with C++" workload, or build with build.sh and g++.
exit /b 1
