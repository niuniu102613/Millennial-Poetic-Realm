# 千年诗境 · Millennial Poetic Realm

> 中文 | [English](#english)

基于 Hadoop MapReduce + Spring Boot + ECharts 的 **十万级古典诗词大数据可视化平台**。
对 `data/clean_data.csv`（109,642 首诗词）进行朝代、作者、文本、意象、类别五个维度的统计分析，结果通过 REST API 提供给前端页面渲染。

---

## 一、技术栈

| 层 | 技术 |
|---|---|
| 数据处理 | Hadoop MapReduce 3.3.6、HBase 2.4.17 |
| 后端 | Java 21、Spring Boot 3.x、Maven 多模块 |
| 前端 | HTML / CSS / 原生 JavaScript、ECharts 5.4（本地部署，无外网 CDN 依赖） |
| 数据 | `data/clean_data.csv`，10.9 万首，覆盖唐/宋/元/清/东汉/战国等朝代 |

## 二、项目结构

```
Millennial-Poetic-Realm/
├── data/                          # 统一数据源（全组共用，不拆分）
│   ├── clean_data.csv             # 十万+ 诗词原始数据
│   └── compute_*.py               # 本地预聚合脚本（生成 JSON 供后端）
├── mr/                            # MapReduce 作业（每人 4 个 Job）
│   ├── member1/                   # 组长：朝代分析 + HBase 导入
│   ├── member2/                   # 作者分析（诗人图谱）
│   ├── member3/                   # 诗词文本分析（高频字/词、长度分布）
│   ├── member4/                   # 诗词意象分析（月/柳/酒等 20 个意象）
│   └── member5/                   # 诗词类别/结构分析（数据概览）
├── backend/                       # Spring Boot 后端（8080 端口）
│   └── src/main/
│       ├── java/com/poetry/backend/
│       │   ├── BackendApplication.java
│       │   ├── controller/        # Dynasty/Author/Text/Imagery/Overview 五个 Controller
│       │   └── service/           # 预聚合 JSON 读取服务
│       └── resources/
│           ├── static/            # 前端静态文件（HTML/JS/CSS）
│           └── *_stats.json        # MapReduce / Python 预聚合结果
├── frontend/                      # 前端页面源码
│   ├── index.html                 # 首页
│   ├── dynasty.html               # 朝代分析
│   ├── poet.html                  # 诗人图谱
│   ├── text.html                  # 诗词文本分析
│   ├── imagery.html               # 诗境意象
│   ├── overview.html              # 诗词数据概览
│   └── js/                        # common.js（i18n 中英文切换）、echarts 本地库
├── hbase/                         # HBase 表设计与导入工具（已并入 member1）
├── pom.xml                        # Maven 聚合根，统一 hadoop/hbase 版本
└── 启动后端.bat                    # 一键启动脚本
```

## 三、快速启动

**前置环境**：JDK 21、Maven 3.6+、（可选）Hadoop 3.3.6 集群。

```bash
# 启动后端
启动后端.bat
# 或
cd backend && mvn spring-boot:run
```

浏览器打开 `http://localhost:8080/index.html`（不要用 file:// 直接打开 HTML）。

## 四、核心页面

| 页面 | 路由 | 内容 |
|---|---|---|
| 首页 | `/index.html` | 项目介绍 + 五大分析入口 |
| 朝代分析 | `/dynasty.html` | 各朝代诗词数量、作者数、平均长度、TopN |
| 诗人图谱 | `/poet.html` | 作者作品数 TopN、各朝代代表诗人 |
| 文本分析 | `/text.html` | 高频字、高频词、长度分布、各朝代高频字 |
| 诗境意象 | `/imagery.html` | 词云、意象 Top10、朝代×意象、诗人×意象环形图 |
| 数据概览 | `/overview.html` | 类别统计、类别 TopN、朝代×类别、长度区间 |

右下角按钮可切换 **中文 / English**，图表坐标轴与朝代名自动翻译。

## 五、REST API

```
GET /api/dynasty/poemCount         # 各朝代诗词数量
GET /api/dynasty/authorCount       # 各朝代作者数量
GET /api/dynasty/avgLen            # 各朝代平均诗词长度
GET /api/dynasty/top/{n}           # 各朝代 TopN
GET /api/author/workTopN           # 作者作品数量 TopN
GET /api/author/avgLenTopN         # 作者平均作品长度 TopN
GET /api/author/dynastyTop         # 各朝代代表作者
GET /api/text/charFreq             # 高频字统计
GET /api/text/wordFreq             # 高频词统计
GET /api/text/lengthDist           # 诗词长度分布
GET /api/text/dynastyChar          # 各朝代高频字
GET /api/imagery/meta              # 意象元信息
GET /api/imagery/total             # 意象出现次数
GET /api/imagery/topn              # 意象 TopN
GET /api/imagery/by-dynasty        # 朝代×意象
GET /api/imagery/by-author         # 作者×意象
GET /api/overview/category         # 各类别数量
GET /api/overview/dynastyCategory  # 朝代×类别
GET /api/overview/lengthRange     # 长度区间分布
```

## 六、组员分工

| 成员 | 职责 |
|---|---|
| 1 号（组长） | 朝代分析、HBase 设计、Spring Boot 后端、REST API、前后端整合、GitHub 仓库 |
| 2 号 | 作者分析、诗人图谱页面 |
| 3 号 | 诗词文本分析、文本分析页面 |
| 4 号 | 诗词意象分析、诗境意象页面 |
| 5 号 | 诗词类别/结构分析、数据概览页面 |

每位组员完成至少 4 个 MapReduce Job，统一读取 HDFS 上的 `clean_data.csv`，结果输出到 HDFS 后导入 HBase。

---

<a id="english"></a>
# Millennial Poetic Realm

> [中文](#) | English

A **big-data visualization platform for 100,000+ classical Chinese poems**, built on Hadoop MapReduce + Spring Boot + ECharts.
It analyzes `data/clean_data.csv` (109,642 poems) across five dimensions — dynasty, author, text, imagery, and category — and serves the results via REST APIs rendered by the frontend.

## Tech Stack

| Layer | Technology |
|---|---|
| Processing | Hadoop MapReduce 3.3.6, HBase 2.4.17 |
| Backend | Java 21, Spring Boot 3.x, Maven multi-module |
| Frontend | HTML / CSS / vanilla JS, ECharts 5.4 (local build, no CDN) |
| Data | `data/clean_data.csv`, 109,642 poems across Tang/Song/Yuan/Qing/Eastern Han/Warring States |

## Project Structure

```
Millennial-Poetic-Realm/
├── data/                          # Shared dataset (single source, not split)
│   ├── clean_data.csv             # 100k+ raw poems
│   └── compute_*.py               # Local pre-aggregation scripts (JSON for backend)
├── mr/                            # MapReduce jobs (4 per member)
│   ├── member1/                   # Lead: dynasty analysis + HBase import
│   ├── member2/                   # Author analysis (poet gallery)
│   ├── member3/                   # Text analysis (top chars/words, length dist.)
│   ├── member4/                   # Imagery analysis (moon/willow/wine, 20 imagery)
│   └── member5/                   # Category/structure analysis (overview)
├── backend/                       # Spring Boot backend (port 8080)
│   └── src/main/
│       ├── java/com/poetry/backend/
│       │   ├── BackendApplication.java
│       │   ├── controller/        # Dynasty / Author / Text / Imagery / Overview
│       │   └── service/           # Pre-aggregated JSON readers
│       └── resources/
│           ├── static/            # Frontend static files (HTML/JS/CSS)
│           └── *_stats.json       # MapReduce / Python aggregated results
├── frontend/                      # Frontend pages
│   ├── index.html                 # Home
│   ├── dynasty.html               # Dynasty analysis
│   ├── poet.html                  # Poet gallery
│   ├── text.html                  # Text analysis
│   ├── imagery.html               # Imagery
│   ├── overview.html              # Data overview
│   └── js/                        # common.js (i18n), local echarts
├── hbase/                         # HBase schema & import tools (merged into member1)
├── pom.xml                        # Maven aggregator, unified hadoop/hbase versions
└── 启动后端.bat                    # One-click start script
```

## Quick Start

**Prerequisites**: JDK 21, Maven 3.6+, (optional) Hadoop 3.3.6 cluster.

```bash
# Start backend
启动后端.bat
# or
cd backend && mvn spring-boot:run
```

Open `http://localhost:8080/index.html` in your browser (do **not** open HTML via `file://`).

## Pages

| Page | Route | Content |
|---|---|---|
| Home | `/index.html` | Intro + five analysis entries |
| Dynasty | `/dynasty.html` | Poem count, poet count, avg. length, TopN per dynasty |
| Poet Gallery | `/poet.html` | Top authors by works, representative poets per dynasty |
| Text Analysis | `/text.html` | Top chars/words, length distribution, per-dynasty top chars |
| Imagery | `/imagery.html` | Word cloud, imagery Top10, dynasty×imagery, poet×imagery donut |
| Overview | `/overview.html` | Category stats, TopN, dynasty×category, length ranges |

A language toggle in the bottom-right switches **中文 / English**; chart axes and dynasty names translate automatically.

## REST API

```
GET /api/dynasty/poemCount         # Poem count per dynasty
GET /api/dynasty/authorCount       # Author count per dynasty
GET /api/dynasty/avgLen            # Avg. poem length per dynasty
GET /api/dynasty/top/{n}           # TopN per dynasty
GET /api/author/workTopN           # Top authors by work count
GET /api/author/avgLenTopN         # Top authors by avg. work length
GET /api/author/dynastyTop         # Representative authors per dynasty
GET /api/text/charFreq             # Top characters
GET /api/text/wordFreq             # Top words
GET /api/text/lengthDist           # Poem length distribution
GET /api/text/dynastyChar         # Top chars per dynasty
GET /api/imagery/meta              # Imagery metadata
GET /api/imagery/total             # Imagery occurrence counts
GET /api/imagery/topn              # Imagery TopN
GET /api/imagery/by-dynasty        # Dynasty × imagery
GET /api/imagery/by-author         # Author × imagery
GET /api/overview/category         # Category counts
GET /api/overview/dynastyCategory  # Dynasty × category
GET /api/overview/lengthRange     # Length range distribution
```

## Team

| Member | Responsibility |
|---|---|
| #1 (Lead) | Dynasty analysis, HBase design, Spring Boot backend, REST API, integration, GitHub |
| #2 | Author analysis, poet gallery page |
| #3 | Text analysis, text analysis page |
| #4 | Imagery analysis, imagery page |
| #5 | Category/structure analysis, overview page |

Each member delivers at least 4 MapReduce jobs reading `clean_data.csv` from HDFS, writing results back to HDFS and importing into HBase.