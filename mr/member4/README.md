# Millennial Poetic Realm - 意象分析模块（4号成员）
> 对应项目：千年诗境 · 古典诗词大数据分析平台
> 负责角色：4号成员（意象分析方向）
> 模块范围：全链路覆盖 **MapReduce计算 → HBase存储 → Spring Boot后端接口 → 前端可视化页面**，所有代码收拢于本目录，分工边界清晰，可独立运行也可无缝整合进主项目。

---

## 一、目录结构
```
member4/
├── README.md                    # 本说明文档
├── pom.xml                      # MR任务 + HBase入库程序 依赖配置
├── test_data.csv                # 本地测试用小数据集
├── output/                      # 本地MR运行输出结果（git可忽略）
│
├── src/main/java/com/millennial/member4/
│   ├── mr/                      # 【核心】4个意象分析MapReduce任务
│   │   ├── job2/                # 练手：诗词长度区间分布统计 只是测试文件，不是我负责的内容
│   │   ├── job3/                # 意象出现次数统计
│   │   ├── job4/                # 意象TopN排序统计
│   │   ├── job5/                # 朝代×意象交叉统计
│   │   └── job6/                # 作者×意象交叉统计
│   │
│   └── hbase/                   # HBase连接工具 + 批量入库程序
│       ├── HBaseUtil.java       # HBase连接、建表工具类
│       └── loader/
│           ├── ImageryTotalLoader.java
│           ├── ImageryTopNLoader.java
│           ├── ImageryByDynastyLoader.java
│           └── ImageryByAuthorLoader.java
│
├── backend/                     # 【后端】意象分析独立微服务（Spring Boot）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/millennial/member4/backend/
│       │   ├── Member4ImageryApplication.java  # 启动类
│       │   ├── config/HBaseConfig.java          # HBase连接配置
│       │   ├── common/Result.java              # 统一响应封装
│       │   └── imagery/
│       │       ├── controller/ImageryController.java
│       │       └── service/ImageryService.java
│       └── resources/application.yml
│
├── frontend/                    # 【前端】「诗境意象」可视化页面（纯静态）
│   ├── index.html
│   ├── css/style.css
│   └── js/imagery-charts.js
│
└── docs/                        # 运行截图、接口文档、实验记录
```

---

## 二、核心模块说明

### 1. MapReduce 计算任务
基于 Hadoop MapReduce 框架，对全量诗词数据做意象维度的统计计算，输出结果存入 HDFS。

| 任务编号 | 任务名称 | 功能说明 | 输出键值 |
|---------|---------|---------|---------|
| Job3 | 意象出现次数统计 | 统计每个意象在全量诗词中出现的总首数（单首诗内去重） | `<意象名, 出现首数>` |
| Job4 | 意象TopN排序 | 按出现次数降序排序，输出TopN意象 | `<意象名, 出现首数>`（按序输出） |
| Job5 | 朝代×意象交叉统计 | 分朝代统计每个意象的出现首数 | `<朝代_意象名, 出现首数>` |
| Job6 | 作者×意象交叉统计 | 分作者统计每个意象的出现首数 | `<作者名_意象名, 出现首数>` |

**统一规则**：
- 输入数据源：`clean_data.csv`（全项目共用，字段：`poem_id,title,author,dynasty,content,category,source,content_len`）
- 统计粒度：以**首**为单位，同一首诗中同一意象仅计数1次
- 预置意象词典：`月、风、云、山、水、花、鸟、酒、剑、春、秋、江、河、雪、雨、柳、梅、菊、楼、舟`
- 脏数据处理：字段缺失、格式错误的行直接跳过

---

### 2. HBase 数据存储
将 MR 计算结果写入 HBase，为后端接口提供高效查询支撑。

#### 表结构设计
统一命名空间：`poetry`，统一列族：`info`

| 表名 | RowKey设计 | 列 | 用途 |
|-----|-----------|-----|------|
| `poetry:imagery_total` | 意象名称（如：`月`） | `info:count`（出现首数） | 全量意象词频查询，词云图 |
| `poetry:imagery_topn` | 两位序号（如：`00`、`01`） | `info:imagery`、`info:count` | TopN排行榜，按序直接读取 |
| `poetry:imagery_by_dynasty` | `朝代_意象名`（如：`唐代_月`） | `info:count` | 分朝代意象分布查询 |
| `poetry:imagery_by_author` | `作者名_意象名`（如：`李白_月`） | `info:count` | 诗人意象偏好查询 |

#### 入库程序
每个表对应一个独立入库程序，读取 MR 输出的 `part-r-00000` 文件，批量写入 HBase。

---

### 3. 后端接口
基于 Spring Boot 开发的独立微服务，端口默认 `8081`，提供 4 个 REST 接口，返回标准 JSON 格式。

#### 接口清单
| 接口路径 | 请求方式 | 参数 | 功能描述 | 前端用途 |
|---------|---------|------|---------|--------|
| `/api/imagery/total` | GET | 无 | 获取所有意象出现总次数 | 意象词云图 |
| `/api/imagery/topn` | GET | `limit`（默认10） | 获取意象TopN排行榜 | 柱状图、排行榜 |
| `/api/imagery/by-dynasty` | GET | `dynasty`（可选） | 按朝代获取意象分布 | 分朝代对比图 |
| `/api/imagery/by-author` | GET | `author`（可选） | 按作者获取意象偏好 | 诗人意象分析 |

#### 返回示例
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "月": 1286,
        "山": 976,
        "风": 852
    }
}
```

---

### 4. 前端可视化页面
纯 HTML/CSS/JS + ECharts 实现，无框架依赖，可直接运行，也可嵌入主项目前端。

**页面名称**：诗境意象
**包含图表**：
1. 高频意象词云图
2. 意象出现次数Top10柱状图
3. 朝代意象分布柱状图
4. 诗人意象偏好饼图

---

## 三、运行环境要求
- JDK 1.8
- Maven 3.6+
- Hadoop 3.3.x
- HBase 2.4.x
- ZooKeeper 3.8.x

---

## 四、分步运行指南

### 步骤1：运行 MapReduce 任务
```bash
# 1. 上传全量数据到HDFS
hdfs dfs -put clean_data.csv /millennial_poetic/input/

# 2. 打包MR任务
mvn clean package -DskipTests

# 3. 提交意象次数统计任务（示例）
hadoop jar target/member4-mr-jobs.jar \
com.millennial.member4.mr.job3.ImageryCountDriver \
/millennial_poetic/input/clean_data.csv \
/millennial_poetic/output/member4/job3
```

> 其余3个任务替换对应主类和输出路径即可。

### 步骤2：HBase 建表与数据入库
```bash
# 1. 先创建命名空间（HBase Shell中执行）
create_namespace 'poetry'

# 2. 运行入库程序（本地/集群均可）
java -cp target/member4-mr-jobs.jar com.millennial.member4.hbase.loader.ImageryTotalLoader \
192.168.130.133:2181 \
/millennial_poetic/output/member4/job3/part-r-00000
```

> 4个表依次运行对应Loader程序即可。

### 步骤3：启动后端服务
```bash
cd backend
mvn spring-boot:run
```
启动成功后访问：`[http://localhost:8081/api/imagery/total](http://localhost:8081/api/imagery/total)` 验证接口。

### 步骤4：访问前端页面
直接用浏览器打开 `frontend/index.html` 即可查看4个可视化图表。

---

## 五、与主项目整合说明
### MR 部分
Jar 包统一提交集群运行，结果各自入库，无需代码整合。

### 后端部分
- 方案A（独立部署）：本模块后端单独启动，前端通过跨域调用接口
- 方案B（合并部署）：将 `backend/src/main/java` 下的代码包复制进全局后端工程，合并依赖配置，统一启动

### 前端部分
将 `frontend` 文件夹复制进全局前端目录，在主菜单添加「诗境意象」入口，指向 `frontend/index.html` 即可。

---

## 六、注意事项
1. 运行 MR 任务前，输出路径必须不存在，否则报错
2. HBase 启动前必须先启动 ZooKeeper
3. 本地运行 Hadoop 若出现 `winutils` 警告，不影响计算逻辑，可忽略
4. 前端默认请求 `localhost:8081`，部署到服务器需修改 `js/imagery-charts.js` 中的 `BASE_URL`
5. 意象词典可在各 MR 任务的 `IMAGERY_DICT` 数组中自行扩展