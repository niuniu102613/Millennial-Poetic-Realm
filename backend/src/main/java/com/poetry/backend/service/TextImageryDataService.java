package com.poetry.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 文本分析(member3) + 意象分析(member4) 统计数据服务。
 * 从 classpath text_imagery_stats.json 读取（由 CSV 预聚合，等同 MR 结果）。
 * 另从 clean_data.csv 加载原始诗词，用于意象共现矩阵与诗句查询。
 */
@Service
public class TextImageryDataService {

    private JsonNode root;
    private final List<String[]> poems = new ArrayList<>(); // [title, author, dynasty, content]
    private static final Random RANDOM = new Random();

    @PostConstruct
    public void load() {
        // 1. 加载预聚合统计 JSON
        try (InputStream in = getClass().getResourceAsStream("/text_imagery_stats.json")) {
            if (in == null) throw new IllegalStateException("classpath 下找不到 text_imagery_stats.json");
            root = new ObjectMapper().readTree(in);
            System.out.println("[TextImageryDataService] 文本/意象数据加载成功");
        } catch (Exception e) {
            System.err.println("[TextImageryDataService] JSON 加载失败: " + e.getMessage());
            e.printStackTrace();
        }

        // 2. 加载原始 CSV 诗词（用于共现矩阵精确计算 + 诗句查询）
        try (InputStream in = getClass().getResourceAsStream("/clean_data.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            br.readLine(); // 跳过表头
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] cols = parseCsvLine(line);
                if (cols.length >= 5) {
                    // [poem_id, title, author, dynasty, content, category, source, content_len]
                    poems.add(new String[]{cols[1], cols[2], cols[3], cols[4]});
                    count++;
                }
            }
            System.out.println("[TextImageryDataService] 原始诗词 CSV 加载成功，共 " + count + " 首");
        } catch (Exception e) {
            System.err.println("[TextImageryDataService] CSV 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** 简单 CSV 行解析（处理引号包裹的逗号） */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    /* ---- member3 文本分析 ---- */
    public JsonNode charFreq() { return root.path("member3").path("charFreq"); }
    public JsonNode wordFreq() { return root.path("member3").path("wordFreq"); }
    public JsonNode lengthDist() { return root.path("member3").path("lengthDist"); }
    public JsonNode dynastyChar() { return root.path("member3").path("dynastyChar"); }

    /* ---- member4 意象分析 ---- */
    public JsonNode imageryTotal() { return root.path("member4").path("total"); }
    public JsonNode imageryTopN() { return root.path("member4").path("topn"); }
    public JsonNode imageryByDynasty() { return root.path("member4").path("byDynasty"); }
    public JsonNode imageryByAuthor() { return root.path("member4").path("byAuthorTop"); }
    public JsonNode meta() { return root.path("meta"); }

    /* ---- 新增：意象共现矩阵热力图 ---- */
    /**
     * 从原始诗词精确计算 Top10 意象两两共现次数。
     * @param dynasty 朝代名，空字符串表示全部
     */
    public Map<String, Object> correlation(String dynasty) {
        // 1. 取 Top10 高频意象
        JsonNode total = root.path("member4").path("total");
        List<String> labels = new ArrayList<>();
        for (JsonNode node : total) {
            if (labels.size() >= 10) break;
            labels.add(node.path("imagery").asText());
        }

        // 2. 遍历诗词，统计共现
        int n = labels.size();
        int[][] matrix = new int[n][n];
        for (String[] poem : poems) {
            String poemDynasty = poem[2];
            String content = poem[3];
            if (content == null || content.isEmpty()) continue;
            if (dynasty != null && !dynasty.isEmpty() && !dynasty.equals(poemDynasty)) continue;

            // 检测这首诗包含哪些意象
            List<Integer> hitIdx = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (content.contains(labels.get(i))) hitIdx.add(i);
            }
            // 两两共现 +1
            for (int a = 0; a < hitIdx.size(); a++) {
                for (int b = 0; b < hitIdx.size(); b++) {
                    matrix[hitIdx.get(a)][hitIdx.get(b)]++;
                }
            }
        }

        // 3. 组装返回
        List<List<Integer>> data = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < n; j++) row.add(matrix[i][j]);
            data.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    /* ---- 新增：根据诗人+意象查找诗句 ---- */
    /**
     * 从原始诗词中找该诗人写的、包含该意象的诗句，随机返回一首。
     */
    public Map<String, String> poetSentence(String poet, String imagery) {
        List<String[]> matches = new ArrayList<>();
        for (String[] poem : poems) {
            if (poet.equals(poem[1]) && poem[3] != null && poem[3].contains(imagery)) {
                matches.add(poem);
            }
        }
        Map<String, String> result = new LinkedHashMap<>();
        if (matches.isEmpty()) {
            result.put("sentence", "暂无匹配诗句");
            result.put("title", "—");
            return result;
        }
        String[] picked = matches.get(RANDOM.nextInt(matches.size()));
        // 取第一句作为展示（按句号/逗号切分）
        String content = picked[3];
        String firstSentence = content;
        int endIdx = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '。' || c == '！' || c == '？') {
                endIdx = i + 1;
                break;
            }
        }
        if (endIdx > 0) firstSentence = content.substring(0, endIdx);
        result.put("sentence", firstSentence);
        result.put("title", picked[0]);
        return result;
    }
}
