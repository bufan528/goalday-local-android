[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$content = Get-Content 'c:\Users\bf410\goalday-local\ui_hb.xml' -Raw -Encoding UTF8
$matches = [regex]::Matches($content, 'text="([^"]*)"[^>]*bounds="([^"]+)"')
$matches | ForEach-Object {
    $text = $_.Groups[1].Value
    $bounds = $_.Groups[2].Value
    if ($text -ne "") {
        "$text | $bounds"
    }
} | Select-Object -First 40
