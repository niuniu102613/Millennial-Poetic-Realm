# -*- coding: utf-8 -*-
"""
从 data/clean_data.csv 计算 member3(文本分析) + member4(意象分析) 的统计，
输出 text_imagery_stats.json 供 Spring Boot classpath 读取（HBase 未开机时的本地数据源）。
"""
import csv, json, re, os, collections, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(ROOT, "data", "clean_data.csv")
OUT_PATH = os.path.join(ROOT, "backend", "src", "main", "resources", "text_imagery_stats.json")

IMAGERY_DICT = ["月","风","云","山","水","花","鸟","酒","剑",
                "春","秋","江","河","雪","雨","柳","梅","菊","楼","舟"]

STOP = set("的了是不在有人我他这中上来时也都就一而之以于其且则乃焉兮如何似共又复莫无未自但虽纵却须即为因缘故在是了着过与及或此该等上下内外间时处之中")

# jieba 可选
try:
    import jieba
    jieba.setLogLevel(20)
    HAS_JIEBA = True
except Exception:
    HAS_JIEBA = False

ch_re = re.compile(r"[\u4e00-\u9fff]")

char_counter = collections.Counter()           # member3 job1 高频字
word_counter = collections.Counter()           # member3 job2 高频词
len_buckets = collections.Counter()           # member3 job3 长度区间
dynasty_chars = collections.defaultdict(collections.Counter)  # job4 朝代高频字

imagery_total = collections.Counter()           # member4 job3 意象总次数
dynasty_imag = collections.defaultdict(collections.Counter)  # job5 朝代×意象
author_imag = collections.defaultdict(collections.Counter) # job6 作者×意象

dynasties = collections.Counter()
authors = set()

def bucket(n):
    if n < 20: return "<20"
    if n < 40: return "20-39"
    if n < 60: return "40-59"
    if n < 80: return "60-79"
    if n < 100: return "80-99"
    if n < 150: return "100-149"
    if n < 200: return "150-199"
    return ">=200"

with open(CSV_PATH, encoding="utf-8") as f:
    r = csv.reader(f)
    header = next(r)
    for row in r:
        if len(row) < 8: continue
        pid, title, author, dynasty, content, category, source, clen = row[:8]
        content = content.strip()
        if not content: continue
        try:
            L = int(clen)
        except:
            L = len(content)

        # member3 高频字
        for ch in ch_re.findall(content):
            char_counter[ch] += 1
        # 朝代高频字
        if dynasty:
            for ch in ch_re.findall(content):
                dynasty_chars[dynasty][ch] += 1

        # member3 高频词
        if HAS_JIEBA:
            for w in jieba.cut(content):
                w = w.strip()
                if len(w) < 2: continue
                if not ch_re.search(w): continue
                if w in STOP: continue
                word_counter[w] += 1
        else:
            # 退化：二元相邻汉字对
            chars = ch_re.findall(content)
            for i in range(len(chars)-1):
                w = chars[i]+chars[i+1]
                if w in STOP: continue
                word_counter[w] += 1

        # member3 长度区间
        len_buckets[bucket(L)] += 1

        # member4 意象（一首诗一个意象计一次）
        hit = {im for im in IMAGERY_DICT if im in content}
        for im in hit:
            imagery_total[im] += 1
            if dynasty:
                dynasty_imag[dynasty][im] += 1
            if author:
                author_imag[author][im] += 1

        if dynasty: dynasties[dynasty] += 1
        if author: authors.add(author)

# ---- 整理成 JSON ----
order = ["<20","20-39","40-59","60-79","80-99","100-149","150-199",">=200"]
result = {
    "member3": {
        "charFreq": [{"char":c,"count":n} for c,n in char_counter.most_common(30)],
        "wordFreq": [{"word":w,"count":n} for w,n in word_counter.most_common(30)],
        "lengthDist": [{"range":b,"count":len_buckets.get(b,0)} for b in order],
        "dynastyChar": {
            d:[{"char":c,"count":n} for c,n in cc.most_common(15)]
            for d,cc in dynasty_chars.items()
        }
    },
    "member4": {
        "total": [{"imagery":im,"count":n} for im,n in imagery_total.most_common()],
        "topn": [{"imagery":im,"count":n} for im,n in imagery_total.most_common(10)],
        "byDynasty": {d:[{"imagery":im,"count":n} for im,n in cc.most_common()]
                      for d,cc in dynasty_imag.items()},
        "byAuthorTop": {},
    },
    "meta": {
        "poemCount": sum(dynasties.values()),
        "dynastyCount": len(dynasties),
        "authorCount": len(authors),
        "imageryDictSize": len(IMAGERY_DICT),
        "jieba": HAS_JIEBA
    }
}

# Top20 高产作者
top20 = sorted(author_imag.items(), key=lambda x:-sum(x[1].values()))[:20]
by_author = {a:[{"imagery":im,"count":n} for im,n in cc.most_common(8)] for a,cc in top20}
# 强制补入指定名诗人（即使未进 Top20）
FAMOUS = ["李白","杜甫","苏轼","李清照"]
for a in FAMOUS:
    if a in author_imag:
        by_author[a] = [{"imagery":im,"count":n} for im,n in author_imag[a].most_common(8)]
    else:
        print("WARN: 数据集中未找到诗人", a)
result["member4"]["byAuthorTop"] = by_author

with open(OUT_PATH, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print("OK jieba=", HAS_JIEBA)
print("poems=", sum(dynasties.values()), "authors=", len(authors))
print("chars top5:", char_counter.most_common(5))
print("words top5:", word_counter.most_common(5))
print("imagery:", imagery_total.most_common())
print("out:", OUT_PATH)
