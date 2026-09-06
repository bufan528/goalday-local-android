$files = Get-ChildItem -Path 'c:\Users\bf410\goalday-local' -Recurse -Filter 'HandbookScheduleSurface.kt' -ErrorAction SilentlyContinue
foreach ($f in $files) {
    Write-Output "=== $($f.FullName) ==="
    $matches = Select-String -Path $f.FullName -Pattern 'padStart|width\(12\.25|width\(24\.5|vertical = 1\.75|vertical = 3\.5'
    foreach ($m in $matches) {
        Write-Output "  Line $($m.LineNumber): $($m.Line.Trim())"
    }
}
