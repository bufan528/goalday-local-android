$bytes = [System.IO.File]::ReadAllBytes('c:\Users\bf410\goalday-local\docs\ui_dump7.xml')
$c = [System.Text.Encoding]::UTF8.GetString($bytes)
if ($c -match 'TEST') { Write-Output "FOUND TEST marker - code IS being executed" } else { Write-Output "NOT found TEST marker" }
$pattern = 'text="([^"]*)"[^>]*clickable="true"[^>]*bounds="(\[\d+,\d+\]\[\d+,\d+\])"'
$matches = [regex]::Matches($c, $pattern)
Write-Output "---clickable elements---"
foreach ($m in $matches) {
    $text = $m.Groups[1].Value
    $bounds = $m.Groups[2].Value
    if ($text.Length -gt 0) {
        Write-Output "$text => $bounds"
    }
}
