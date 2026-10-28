#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从 clean_data.csv 生成 overview_stats.json（member5 数据）"""
import csv, json, os
from collections import defaultdict

csv_path = os.path.join(os.path.dirname(__file__), '..', 'data', 'clean_data.csv')
out_path = os.path.join(os.path.dirname(__file__), '..', 'backend', 'src', 'main', 'resources', 'overview_stats.json')

cat_count = defaultdict(int)
dyn_cat = defaultdict(lambda: defaultdict(int))
len_count = defaultdict(int)

# 长度区间定义
ranges = [(0,20,'0-20'),(21,50,'21-50'),(51,100,'51-100'),(101,200,'101-200'),(201,9999,'200+')]

def norm_cat(c):
    """把细分类别归为大类"""
    c = c.strip()
    if c.startswith('宋词') or c.startswith('唐词') or c in ('词','曲子词'):
        return '词'
    if '曲' in c:
        return '曲'
    if '绝句' in c:
        return '绝句'
    if '律诗' in c or '排律' in c:
        return '律诗'
    if '古体' in c or '七言古' in c or '五言古' in c or '乐府' in c or c == '楚辞':
        return '古体诗'
    if '近体' in c:
        return '近体诗'
    return '其他'

def get_range(l):
    for lo,hi,name in ranges:
        if lo <= l <= hi:
            return name
    return '200+'

with open(csv_path, encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        cat = norm_cat(row['category'])
        dyn = row['dynasty'].strip()
        try:
            ln = int(row['content_len'])
        except:
            continue
        if cat:
            cat_count[cat] += 1
        if dyn and cat:
            dyn_cat[dyn][cat] += 1
        len_count[get_range(ln)] += 1

# 朝代顺序
dyn_order = ['先秦','魏晋','唐代','五代','宋代','元代','明代','清代']
dyn_list = []
for d in dyn_order:
    if d in dyn_cat:
        dyn_list.append({'dynasty': d, 'categories': dict(dyn_cat[d])})

result = {
    'categoryCount': [{'category': k, 'count': v} for k,v in sorted(cat_count.items(), key=lambda x:-x[1])],
    'lengthRange': [{'range': name, 'count': len_count.get(name,0)} for _,_,name in ranges],
    'dynastyCategory': dyn_list
}

with open(out_path, 'w', encoding='utf-8') as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f'生成完成: {out_path}')
print(f'类别数: {len(cat_count)}, 朝代数: {len(dyn_list)}, 总长诗词数: {sum(cat_count.values())}')
