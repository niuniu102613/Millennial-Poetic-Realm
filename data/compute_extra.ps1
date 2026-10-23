# Recompute with broad category buckets for clean visualization
$csv = "D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\data\clean_data.csv"
$rows = Import-Csv $csv -Encoding UTF8

function Get-Bucket($cat){
    if([string]::IsNullOrWhiteSpace($cat)){ return "其他" }
    if($cat -eq "古体诗"){ return "古体诗" }
    if($cat.StartsWith("宋词") -or $cat -like "*词*"){ return "词" }
    if($cat -like "*曲*"){ return "曲" }
    if($cat -like "*古诗*"){ return "古诗" }
    return "其他"
}

$catCount = @{}
$dynCat = @{}
$lenOrder = @("0-20","21-50","51-100","101-200","200+")
$lenRange = [ordered]@{ "0-20"=0; "21-50"=0; "51-100"=0; "101-200"=0; "200+"=0 }
$authorWorks = @{}
$authorLenSum = @{}
$authorLenCnt = @{}
$byDynAuthor = @{}

foreach($r in $rows){
    $cat = $r.category.Trim()
    $dyn = $r.dynasty.Trim()
    $author = $r.author.Trim()
    $len = 0
    [int]::TryParse($r.content_len, [ref]$len) | Out-Null
    $bucket = Get-Bucket $cat

    $catCount[$bucket] = 1 + [int]$catCount[$bucket]
    if($dyn){
        if(-not $dynCat.ContainsKey($dyn)){ $dynCat[$dyn] = @{} }
        $dynCat[$dyn][$bucket] = 1 + [int]$dynCat[$dyn][$bucket]
    }
    if($len -le 20){ $lenRange["0-20"]++ }
    elseif($len -le 50){ $lenRange["21-50"]++ }
    elseif($len -le 100){ $lenRange["51-100"]++ }
    elseif($len -le 200){ $lenRange["101-200"]++ }
    else { $lenRange["200+"]++ }

    if($author){
        $authorWorks[$author] = 1 + [int]$authorWorks[$author]
        $authorLenSum[$author] = [int]$authorLenSum[$author] + $len
        $authorLenCnt[$author] = 1 + [int]$authorLenCnt[$author]
        if(-not $byDynAuthor.ContainsKey($dyn)){ $byDynAuthor[$dyn] = @{} }
        $byDynAuthor[$dyn][$author] = 1 + [int]$byDynAuthor[$dyn][$author]
    }
}

$catOrder = @("古体诗","古诗","词","曲","其他")
$catList = foreach($c in $catOrder){ if($catCount[$c]){ @{category=$c; count=$catCount[$c]} } }
$lenList = foreach($k in $lenOrder){ @{range=$k; count=$lenRange[$k]} }
$dynCatList = foreach($dyn in ($dynCat.Keys | Sort-Object)){
    $cats = [ordered]@{}
    foreach($c in $catOrder){ if($dynCat[$dyn][$c]){ $cats[$c] = $dynCat[$dyn][$c] } }
    @{ dynasty=$dyn; categories=$cats }
}
$workTop = $authorWorks.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 20 | ForEach-Object { @{author=$_.Key; count=$_.Value} }
$avgTop = foreach($a in $authorLenCnt.Keys){
    if([int]$authorLenCnt[$a] -ge 5){
        [PSCustomObject]@{ author=$a; avgLen=[math]::Round([int]$authorLenSum[$a]/[int]$authorLenCnt[$a],1); count=[int]$authorLenCnt[$a] }
    }
}
$avgTop = $avgTop | Sort-Object avgLen -Descending | Select-Object -First 20 | ForEach-Object { @{author=$_.author; avgLen=$_.avgLen; count=$_.count} }
$dynAuthorOut = [ordered]@{}
foreach($dyn in ($byDynAuthor.Keys | Sort-Object)){
    $top = $byDynAuthor[$dyn].GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 5 | ForEach-Object { @{author=$_.Key; count=$_.Value} }
    $dynAuthorOut[$dyn] = $top
}

$result = [ordered]@{
    categoryCount = @($catList)
    lengthRange = @($lenList)
    dynastyCategory = @($dynCatList)
    authorWorkTopN = @($workTop)
    authorAvgLenTopN = @($avgTop)
    dynastyTopAuthors = $dynAuthorOut
}
$out = "D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\data\result\extra_stats.json"
$result | ConvertTo-Json -Depth 7 | Out-File $out -Encoding utf8
Write-Host "written: $out"
Write-Host "=== category buckets ==="; $catList | ForEach-Object { "$($_.category)=$($_.count)" }
Write-Host "=== dynasties in stack ==="; $dynCatList | ForEach-Object { $_.dynasty }
