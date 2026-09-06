$pkg = "com.bf410.goaldaylocal"
$activity = "$pkg/.MainActivity"

function cap($name) {
    adb shell screencap -p /sdcard/$name.png
    adb pull /sdcard/$name.png c:\Users\bf410\goalday-local\$name.png
}

adb shell am force-stop $pkg
Start-Sleep -Seconds 2
adb shell am start -n $activity --es goalday_start_target handbook
Start-Sleep -Seconds 8

cap "tab_list"

adb shell input tap 844 151
Start-Sleep -Seconds 4
cap "tab_week"

adb shell input tap 968 151
Start-Sleep -Seconds 4
cap "tab_month"

adb shell input tap 1114 151
Start-Sleep -Seconds 4
cap "tab_diary"

Write-Host "Done capturing tabs"
