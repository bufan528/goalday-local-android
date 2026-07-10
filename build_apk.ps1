cd c:\Users\bf410\goalday-local
.\gradlew.bat assembleDebug
Copy-Item -Path "app\build\outputs\apk\debug\app-debug.apk" -Destination "D:\Downloads\goalday-debug.apk" -Force
