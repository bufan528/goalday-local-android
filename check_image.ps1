Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile('c:\Users\bf410\goalday-local\docs\screen_book_idle2_20260713.png')
$region = New-Object System.Drawing.Rectangle(480, 250, 852, 1500)
$bmp = New-Object System.Drawing.Bitmap($region.Width, $region.Height)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.DrawImage($img, 0, 0, $region, [System.Drawing.GraphicsUnit]::Pixel)
$bmp.Save('c:\Users\bf410\goalday-local\docs\screen_book_crop.png', [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose(); $img.Dispose()
Write-Host 'cropped'
