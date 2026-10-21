# Member5 —— 诗词类型/结构分析

负责页面：「诗词数据概览」

## 数据格式

输入：`data/clean_data.csv`（UTF-8，CSV，共 8 列）

| 列号 | 字段 | 说明 |
|------|------|------|
| 0 | poem_id | 诗词ID |
| 1 | title | 标题 |
| 2 | author | 作者 |
| 3 | dynasty | 朝代 |
| 4 | content | 正文（可能含中文标点） |
| 5 | category | 诗词类别（古体诗、近体诗等） |
| 6 | source | 出处 |
| 7 | content_len | 正文字数 |

> 注意：content 里是中文逗号「，」不是英文逗号「,」，所以 split(",") 不会切开 content。
> 但代码仍然从末尾取字段（category = 倒数第3列），即使 content 里混入英文逗号也不会错位。

## 4 个 MapReduce Job

### Job 1：各诗词类别数量统计 — `CategoryCount`

- 输入：HDFS 上的 clean_data.csv
- 输出：`category \t count`
- 用途：饼图/柱状图展示各类别占比

### Job 2：诗词类别 TopN — `CategoryTopN`

- 输出：`count \t category`（按 count 降序）
- 第三个参数可选：TopN 数量，默认 10
- 用途：条形图展示最常见的类别

### Job 3：朝代 × 类别统计 — `DynastyCategoryCount`

- 输出：`dynasty \t category \t count`（用制表符分隔，方便前端拆解）
- 用途：堆叠柱状图，对比各朝代的类别分布

### Job 4：诗词长度区间统计 — `PoemLengthRange`

- 直接使用数据自带的 `content_len` 字段，口径统一
- 区间划分：`0-20`、`21-50`、`51-100`、`101-200`、`200+`
- 输出：`range \t count`
- 用途：直方图展示诗词长度分布

## 打包与运行

### 1. 本地编译测试

```bash
mvn clean test
```

### 2. 打包

```bash
mvn clean package -DskipTests
# 产物：target/poetry-mr-1.0-SNAPSHOT.jar
```

### 3. 上传数据到 HDFS（如果还没传）

```bash
hdfs dfs -mkdir -p /poetry/input
hdfs dfs -put ../../data/clean_data.csv /poetry/input/
```

### 4. 运行各个 Job

```bash
# Job 1: 类别数量
hadoop jar target/poetry-mr-1.0-SNAPSHOT.jar com.member5.CategoryCount \
    /poetry/input/clean_data.csv /poetry/output/category_count

# Job 2: 类别 TopN（默认Top10）
hadoop jar target/poetry-mr-1.0-SNAPSHOT.jar com.member5.CategoryTopN \
    /poetry/input/clean_data.csv /poetry/output/category_topn

# Job 2 也可以指定 TopN，比如 Top5
hadoop jar target/poetry-mr-1.0-SNAPSHOT.jar com.member5.CategoryTopN \
    /poetry/input/clean_data.csv /poetry/output/category_topn5 5

# Job 3: 朝代×类别
hadoop jar target/poetry-mr-1.0-SNAPSHOT.jar com.member5.DynastyCategoryCount \
    /poetry/input/clean_data.csv /poetry/output/dynasty_category

# Job 4: 长度区间
hadoop jar target/poetry-mr-1.0-SNAPSHOT.jar com.member5.PoemLengthRange \
    /poetry/input/clean_data.csv /poetry/output/length_range
```

### 5. 查看结果

```bash
hdfs dfs -cat /poetry/output/category_count/part-r-00000
```

## 输出样例

category_count:
```
古体诗	52341
近体诗	38920
词	8200
...
```

dynasty_category:
```
唐代	古体诗	18230
唐代	近体诗	15600
宋代	古体诗	9800
...
```

length_range:
```
0-20	12000
21-50	45000
51-100	32000
101-200	15000
200+	5643
```
