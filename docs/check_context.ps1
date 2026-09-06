$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump5.xml' -Raw -Encoding UTF8
# Find all text="1" occurrences and show surrounding context
$pattern = 'text="1"'
$idx = 0
while ($idx -lt $c.Length) {
    $pos = $c.IndexOf($pattern, $idx)
    if ($pos -lt 0) { break }
    $start = [Math]::Max(0, $pos - 200)
    $end = [Math]::Min($c.Length, $pos + 300)
    $context = $c.Substring($start, $end - $start)
    Write-Output "=== Match at position $pos ==="
    Write-Output $context
    Write-Output ""
    $idx = $pos + $pattern.Length
}
