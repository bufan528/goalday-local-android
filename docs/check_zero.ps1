$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump5.xml' -Raw -Encoding UTF8
# Search for "01" or "02" in text attributes
if ($c -match 'text="01"') { Write-Output "Found text=01" } else { Write-Output "NOT found text=01" }
if ($c -match 'text="02"') { Write-Output "Found text=02" } else { Write-Output "NOT found text=02" }
if ($c -match 'text="03"') { Write-Output "Found text=03" } else { Write-Output "NOT found text=03" }
# Search for standalone "1" in text attributes
$ones = [regex]::Matches($c, 'text="(1)"')
Write-Output "Standalone text=1 count: $($ones.Count)"
# Show context around first text="1"
if ($c -match '(.{0,80})text="1"(.{0,120})') {
    Write-Output "Context: ...$($Matches[1])text=`"1`"$($Matches[2])..."
}
