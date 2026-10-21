# -*- coding: utf-8 -*-
import csv, json, os
from collections import defaultdict

csv_path = r"D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\data\clean_data.csv"
out_dir = r"D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\data\result"
os.makedirs(out_dir, exist_ok=True)

count = defaultdict(int)
authors = defaultdict(set)
len_sum = defaultdict(float)

with open(csv_path, encoding="utf-8") as f:
    reader = csv.reader(f)
    header = next(reader)
    for row in reader:
        if len(row) < 8:
            continue
        dynasty = row[3].strip()
        author = row[2].strip()
        try:
            clen = float(row[7])
        except ValueError:
            continue
        if not dynasty:
            continue
        count[dynasty] += 1
        authors[dynasty].add(author)
        len_sum[dynasty] += clen

# 按诗词数量降序排
dynasties = sorted(count.keys(), key=lambda d: count[d], reverse=True)

result = {
    "poemCount": [{"dynasty": d, "count": count[d]} for d in dynasties],
    "authorCount": [{"dynasty": d, "count": len(authors[d])} for d in dynasties],
    "avgLen": [{"dynasty": d, "avgLen": round(len_sum[d] / count[d], 1)} for d in dynasties],
    "totalPoems": sum(count.values()),
}

out_path = os.path.join(out_dir, "dynasty_stats.json")
with open(out_path, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print("各朝代数量(前10):")
for d in dynasties[:10]:
    print(f"  {d}: 诗词{count[d]} 作者{len(authors[d])} 平均长度{round(len_sum[d]/count[d],1)}")
print("总数:", result["totalPoems"])
print("已写出:", out_path)
