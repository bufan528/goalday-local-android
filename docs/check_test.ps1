$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump6.xml' -Raw -Encoding UTF8
if ($c -match 'TEST') { Write-Output "FOUND TEST - code IS being executed" } else { Write-Output "NOT found TEST - different code path" }
if ($c -match 'text="01"') { Write-Output "Found text=01" } else { Write-Output "NOT found text=01" }
# Check for the em dash using unicode escape
$emdash = [char]0x2014
if ($c.Contains("text=`"$emdash`"")) { Write-Output "Found em-dash (old divider)" } else { Write-Output "NOT found em-dash" }
