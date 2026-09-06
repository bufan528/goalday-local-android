$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump4.xml' -Raw -Encoding UTF8
$pattern = 'text="([^"]*)"[^>]*clickable="true"[^>]*bounds="(\[\d+,\d+\]\[\d+,\d+\])"'
$matches = [regex]::Matches($c, $pattern)
foreach ($m in $matches) {
    $text = $m.Groups[1].Value
    $bounds = $m.Groups[2].Value
    if ($text.Length -gt 0) {
        Write-Output "$text => $bounds"
    }
}
