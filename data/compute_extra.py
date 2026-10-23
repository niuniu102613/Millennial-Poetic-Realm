# -*- coding: utf-8 -*-
import csv, json, collections

csv_path = r"D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\data\clean_data.csv"
out_path = r"D:\dev\workspace\lworkspace\Millennial-Poetic-Realm\data\result\extra_stats.json"

def bucket(cat):
    if not cat: return "其他"
    if cat == "古体诗": return "古体诗"
    if "词" in cat: return "词"
    if "曲" in cat: return "曲"
    if "古诗" in cat: return "古诗"
    return "其他"

cat_count = collections.Counter()
dyn_cat = collections.defaultdict(collections.Counter)
len_order = ["0-20","21-50","51-100","101-200","200+"]
len_range = collections.OrderedDict((k,0) for k in len_order)
author_works = collections.Counter()
author_len_sum = collections.Counter()
author_len_cnt = collections.Counter()
by_dyn_author = collections.defaultdict(collections.Counter)

with open(csv_path, encoding="utf-8") as f:
    reader = csv.DictReader(f)
    for r in reader:
        cat = (r.get("category") or "").strip()
        dyn = (r.get("dynasty") or "").strip()
        author = (r.get("author") or "").strip()
        try: length = int(r.get("content_len") or 0)
        except: length = 0
        b = bucket(cat)
        cat_count[b] += 1
        if dyn: dyn_cat[dyn][b] += 1
        if length <= 20: len_range["0-20"] += 1
        elif length <= 50: len_range["21-50"] += 1
        elif length <= 100: len_range["51-100"] += 1
        elif length <= 200: len_range["101-200"] += 1
        else: len_range["200+"] += 1
        if author:
            author_works[author] += 1
            author_len_sum[author] += length
            author_len_cnt[author] += 1
            if dyn: by_dyn_author[dyn][author] += 1

cat_order = ["古体诗","古诗","词","曲","其他"]
category_count = [{"category":c,"count":cat_count[c]} for c in cat_order if cat_count[c]]
length_range = [{"range":k,"count":len_range[k]} for k in len_order]
dynasty_category = []
for dyn in sorted(dyn_cat.keys()):
    cats = collections.OrderedDict((c, dyn_cat[dyn][c]) for c in cat_order if dyn_cat[dyn][c])
    dynasty_category.append({"dynasty":dyn,"categories":cats})

work_top = [{"author":a,"count":n} for a,n in author_works.most_common(20)]
avg_top = []
for a in author_len_cnt:
    if author_len_cnt[a] >= 5:
        avg_top.append({"author":a,"avgLen":round(author_len_sum[a]/author_len_cnt[a],1),"count":author_len_cnt[a]})
avg_top.sort(key=lambda x:-x["avgLen"])
avg_top = avg_top[:20]

dynasty_top_authors = collections.OrderedDict()
for dyn in sorted(by_dyn_author.keys()):
    dynasty_top_authors[dyn] = [{"author":a,"count":n} for a,n in by_dyn_author[dyn].most_common(5)]

result = {
    "categoryCount": category_count,
    "lengthRange": length_range,
    "dynastyCategory": dynasty_category,
    "authorWorkTopN": work_top,
    "authorAvgLenTopN": avg_top,
    "dynastyTopAuthors": dynasty_top_authors,
}
with open(out_path,"w",encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)
print("written:", out_path)
print("category buckets:", category_count)
print("stack dynasties:", [d["dynasty"] for d in dynasty_category])
print("top authors:", work_top[:5])
