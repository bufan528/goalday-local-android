$ErrorActionPreference = "Continue"
$output = & .\gradlew.bat :app:assembleDebug --console=plain --stacktrace 2>&1
$output | Out-File -FilePath build_full_log.txt -Encoding utf8
Write-Host "EXIT_CODE=$LASTEXITCODE"
Write-Host "LOG_LINES=$($output.Count)"
