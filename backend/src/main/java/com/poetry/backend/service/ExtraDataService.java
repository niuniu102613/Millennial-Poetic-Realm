package com.poetry.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 概览(member5) + 作者(member2) 统计数据服务。
 * 从 classpath extra_stats.json 读取（由 CSV 预聚合，等同 MR 结果）。
 * 待 HBase 上线后，把对应 getter 改为查 HBase 即可，Controller 不用动。
 */
@Service
public class ExtraDataService {

    private JsonNode root;

    @PostConstruct
    public void load() {
        try (InputStream in = getClass().getResourceAsStream("/extra_stats.json")) {
            if (in == null) throw new IllegalStateException("classpath 下找不到 extra_stats.json");
            root = new ObjectMapper().readTree(in);
            System.out.println("[ExtraDataService] 概览/作者数据加载成功");
        } catch (Exception e) {
            System.err.println("[ExtraDataService] 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** member5: 各类别数量 [{category,count}] */
    public JsonNode categoryCount() { return root.path("categoryCount"); }
    /** member5: 长度区间 [{range,count}] */
    public JsonNode lengthRange() { return root.path("lengthRange"); }
    /** member5: 朝代×类别 [{dynasty,categories:{cat:n}}] */
    public JsonNode dynastyCategory() { return root.path("dynastyCategory"); }

    /** member2: 作者作品数 TopN [{author,count}] */
    public JsonNode authorWorkTopN() { return root.path("authorWorkTopN"); }
    /** member2: 作者平均长度 TopN [{author,avgLen,count}] */
    public JsonNode authorAvgLenTopN() { return root.path("authorAvgLenTopN"); }
    /** member2: 各朝代代表作者 {dynasty:[{author,count}]} */
    public JsonNode dynastyTopAuthors() { return root.path("dynastyTopAuthors"); }
}
