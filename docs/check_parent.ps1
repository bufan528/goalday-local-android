$c = Get-Content 'c:\Users\bf410\goalday-local\docs\ui_dump5.xml' -Raw -Encoding UTF8
# Find text="1" at bounds containing 175,1125
$pattern = 'text="1"[^>]*bounds="\[175,1125\]'
$idx = $c.IndexOf($pattern)
if ($idx -ge 0) {
    # Get a large context around this match to see parent structure
    $start = [Math]::Max(0, $idx - 1500)
    $end = [Math]::Min($c.Length, $idx + 500)
    $context = $c.Substring($start, $end - $start)
    Write-Output $context
}
