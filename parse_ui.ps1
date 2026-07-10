[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$content = Get-Content 'c:\Users\bf410\goalday-local\ui_handbook.xml' -Raw -Encoding UTF8
$matches = [regex]::Matches($content, 'text="([^"]+)"[^>]*bounds="([^"]+)"')
$matches | ForEach-Object { "$($_.Groups[1].Value) | $($_.Groups[2].Value)" } | Select-Object -First 80
