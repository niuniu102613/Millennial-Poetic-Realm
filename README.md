# 千年诗境 · Millennial-Poetic-Realm

基于 Hadoop 的十万首古诗词大数据可视化平台。

## 项目结构

```
Millennial-Poetic-Realm/
├── data/clean_data.csv      # 统一诗词数据（10万+）
├── mr/                      # MapReduce 作业
│   ├── member1/             # 朝代分析（组长）
│   ├── member2/             # 诗人分析
│   ├── member3/             # 文本分析
│   ├── member4/             # 意象分析
│   └── member5/             # 类型/结构分析（前端负责人）
├── frontend/                # 前端页面（HTML/CSS/JS + ECharts）
├── backend/                 # Spring Boot 后端（组长）
├── hbase/                   # HBase 表设计
└── tools/mock_server.py     # 本地预览服务器
```

## 快速开始

### 1. 预览前端页面
直接双击 `frontend/index.html` 即可，不需要启动任何服务。

### 2. 本地 mock 预览（可选）
```bash
python tools/mock_server.py
# 浏览器打开 http://localhost:8000
```

### 3. 跑 MapReduce 作业
见 `mr/member5/README.md`，各成员目录下均有独立说明。

## 技术栈
- **数据**: HDFS / MapReduce / HBase
- **后端**: Spring Boot / REST API
- **前端**: HTML / CSS / JavaScript / ECharts
- **构建**: Maven

## 成员分工
| 成员 | 负责 |
|------|------|
| 1号（组长） | 朝代分析、Spring Boot后端、HBase、项目整合 |
| 2号 | 诗人分析 |
| 3号 | 诗词文本分析 |
| 4号 | 诗词意象分析 |
| 5号 | 类型/结构分析、前端框架与页面 |
