# Member3 - 诗词文本分析 MapReduce Jobs

## 构建
bash
cd mr/member3
mvn clean package -DskipTests

## Hadoop 运行命令

> 假设 `clean_data.csv` 已上传至 HDFS：`/input/clean_data.csv`

### Job 1：高频字统计
bash 
hadoop jar /root/hadoop/poetry-mr-member3-1.0-SNAPSHOT.jar \
com.poetry.mr.job1.CharFreqDriver \
/input/clean_data.csv /output/member3/high_freq_chars

### Job 2：高频词统计（Bigram）
bash 
hadoop jar /root/hadoop/poetry-mr-member3-1.0-SNAPSHOT.jar \
com.poetry.mr.job2.WordFreqDriver \
/input/clean_data.csv /output/member3/high_freq_words

### Job 3：诗词长度分布
bash 
hadoop jar /root/hadoop/poetry-mr-member3-1.0-SNAPSHOT.jar \
com.poetry.mr.job3.LengthDistDriver \
/input/clean_data.csv /output/member3/poem_length_dist

### Job 4：诗词内容统计
bash 
hadoop jar /root/hadoop/poetry-mr-member3-1.0-SNAPSHOT.jar \
com.poetry.mr.job4.DynastyCharDriver \
/input/clean_data.csv /output/member3/poem_content_dist

## 结果查看
bash hdfs dfs -cat /output/member3/high_freq_chars/part-r-00000 | head -20 hdfs dfs -cat /output/member3/high_freq_words/part-r-00000 | head -20 hdfs dfs -cat /output/member3/poetry_length_dist/part-r-00000 hdfs dfs -cat /output/member3/dynasty_high_freq_chars/part-r-00000 | head -30

> 结果将保存在 HDFS 的 `/output/member3` 目录下

