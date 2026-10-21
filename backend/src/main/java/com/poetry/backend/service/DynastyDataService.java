package com.poetry.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 朝代统计数据服务。
 * 当前从本地 result JSON 读取（虚拟机/HBase 未启动时用于前后端联调）。
 * 待 HBase 集群启动后，把 load() 内部改为读取 HBase 聚合表即可，Controller 不用动。
 */
@Service
public class DynastyDataService {

    private List<Map<String, Object>> poemCount = new ArrayList<>();
    private List<Map<String, Object>> authorCount = new ArrayList<>();
    private List<Map<String, Object>> avgLen = new ArrayList<>();

    @PostConstruct
    public void load() {
        try (InputStream in = getClass().getResourceAsStream("/dynasty_stats.json")) {
            if (in == null) {
                throw new IllegalStateException("classpath 下找不到 dynasty_stats.json");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(in);

            poemCount = parseArray(root.path("poemCount"), "count");
            authorCount = parseArray(root.path("authorCount"), "count");
            avgLen = parseArray(root.path("avgLen"), "avgLen");

            System.out.println("[DynastyDataService] 数据加载成功：朝代数=" + poemCount.size());
        } catch (Exception e) {
            System.err.println("[DynastyDataService] 加载数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** 把 JSON 里的 [{dynasty, count/avgLen}] 解析成 List<Map> */
    private List<Map<String, Object>> parseArray(JsonNode arr, String valueKey) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (arr != null && arr.isArray()) {
            for (JsonNode node : arr) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("dynasty", node.path("dynasty").asText());
                m.put("value", node.path(valueKey).asDouble());
                list.add(m);
            }
        }
        return list;
    }

    public List<Map<String, Object>> getPoemCount() { return poemCount; }
    public List<Map<String, Object>> getAuthorCount() { return authorCount; }
    public List<Map<String, Object>> getAvgLen() { return avgLen; }

    /** 按诗词数量取前 N */
    public List<Map<String, Object>> getTopN(int n) {
        List<Map<String, Object>> sorted = new ArrayList<>(poemCount);
        sorted.sort((a, b) -> Double.compare(
                ((Number) b.get("value")).doubleValue(),
                ((Number) a.get("value")).doubleValue()));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }
}
