param(
    [Parameter(Mandatory=$true)][string]$Serial,
    [string]$TapText = '',
    [string]$SaveXml = ''
)
# Development journey helper. Coordinates are derived from the current UI dump,
# not the runner's fixed coordinate registry.
$dumpResult = adb -s $Serial shell uiautomator dump /sdcard/repro-window.xml
if ($LASTEXITCODE -ne 0 -or ($dumpResult -join "`n") -notmatch 'dumped to') {
    throw 'The phone UI is changing or unavailable. Retry after the screen is ready.'
}
[xml]$phoneUi = (adb -s $Serial shell cat /sdcard/repro-window.xml) -join "`n"
$foreground = $phoneUi.SelectSingleNode('/hierarchy/node')
if (!$foreground -or $foreground.package -notin @('com.pranay.reproai', 'com.pranay.demoshop')) {
    throw 'ReproAI or DemoShop must be foregrounded; unrelated UI was not saved or printed.'
}
if ($SaveXml) { $phoneUi.Save((Join-Path (Get-Location) $SaveXml)) }
if ($TapText) {
    $node = $phoneUi.SelectNodes('//node') | Where-Object { $_.text -eq $TapText } | Select-Object -First 1
    if (!$node) { throw "UI text not found: $TapText" }
    $coords = [regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value }
    $tapX = [int](($coords[0] + $coords[2]) / 2)
    $tapY = [int](($coords[1] + $coords[3]) / 2)
    adb -s $Serial shell input tap $tapX $tapY
} else {
    $phoneUi.SelectNodes('//node[@text!=""]') | Select-Object text,bounds | Format-Table -AutoSize
}
