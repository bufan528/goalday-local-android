$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump5.xml' -Raw -Encoding UTF8
$pattern = 'text="([^"]*)"[^>]*bounds="(\[\d+,\d+\]\[\d+,\d+\])"'
$matches = [regex]::Matches($c, $pattern)
foreach ($m in $matches) {
    $text = $m.Groups[1].Value
    $bounds = $m.Groups[2].Value
    if ($text -match '^\d+$' -or $text -match '^\d{2}$') {
        Write-Output "DAYNUM: $text => $bounds"
    }
}
Write-Output "---clickable---"
$pattern2 = 'text="([^"]*)"[^>]*clickable="true"[^>]*bounds="(\[\d+,\d+\]\[\d+,\d+\])"'
$matches2 = [regex]::Matches($c, $pattern2)
foreach ($m in $matches2) {
    $text = $m.Groups[1].Value
    $bounds = $m.Groups[2].Value
    if ($text.Length -gt 0) {
        Write-Output "$text => $bounds"
    }
}
