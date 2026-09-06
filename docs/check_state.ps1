$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump6.xml' -Raw -Encoding UTF8
$pattern = 'text="([^"]*)"[^>]*clickable="true"[^>]*bounds="(\[\d+,\d+\]\[\d+,\d+\])"'
$matches = [regex]::Matches($c, $pattern)
foreach ($m in $matches) {
    $text = $m.Groups[1].Value
    $bounds = $m.Groups[2].Value
    if ($text.Length -gt 0) {
        Write-Output "$text => $bounds"
    }
}
Write-Output "---all text---"
$pattern2 = 'text="([^"]*)"'
$matches2 = [regex]::Matches($c, $pattern2)
foreach ($m in $matches2) {
    $text = $m.Groups[1].Value
    if ($text.Length -gt 0 -and $text.Length -lt 30) {
        Write-Output $text
    }
}
