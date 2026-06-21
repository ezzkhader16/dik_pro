@echo off
cd /d "%~dp0.."

if not exist target\classes mkdir target\classes
if not exist target\classes\images mkdir target\classes\images

copy /Y src\main\resources\images\map-background.png target\classes\images\ > nul 2> nul

"C:\Program Files\Java\jdk-20\bin\javac.exe" --release 20 --module-path "C:\JavaSDK\javafx-sdk-21.0.10\lib" --add-modules javafx.controls -d target\classes src\main\java\application\*.java
