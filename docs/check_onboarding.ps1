$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_check.xml' -Raw -Encoding UTF8
if ($c -match '跳过') { Write-Output "Still on onboarding (found Skip button)" } else { Write-Output "Onboarding dismissed" }
if ($c -match 'TEST') { Write-Output "FOUND TEST marker" } else { Write-Output "NOT found TEST marker" }
if ($c -match '清单') { Write-Output "Found 清单 tab - on handbook page" } else { Write-Output "NOT found 清单 tab" }
