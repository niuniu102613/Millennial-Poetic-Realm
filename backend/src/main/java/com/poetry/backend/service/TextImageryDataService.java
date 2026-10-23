package com.poetry.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 文本分析(member3) + 意象分析(member4) 统计数据服务。
 * 从 classpath text_imagery_stats.json 读取（由 CSV 预聚合，等同 MR 结果）。
 * 待 HBase 上线后，把对应 getter 改为查 HBase 即可，Controller 不用动。
 */
@Service
public class TextImageryDataService {

    private JsonNode root;

    @PostConstruct
    public void load() {
        try (InputStream in = getClass().getResourceAsStream("/text_imagery_stats.json")) {
            if (in == null) throw new IllegalStateException("classpath 下找不到 text_imagery_stats.json");
            root = new ObjectMapper().readTree(in);
            System.out.println("[TextImageryDataService] 文本/意象数据加载成功");
        } catch (Exception e) {
            System.err.println("[TextImageryDataService] 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ---- member3 文本分析 ---- */
    /** 高频字 [{char,count}] */
    public JsonNode charFreq() { return root.path("member3").path("charFreq"); }
    /** 高频词 [{word,count}] */
    public JsonNode wordFreq() { return root.path("member3").path("wordFreq"); }
    /** 长度分布 [{range,count}] */
    public JsonNode lengthDist() { return root.path("member3").path("lengthDist"); }
    /** 各朝代高频字 {dynasty:[{char,count}]} */
    public JsonNode dynastyChar() { return root.path("member3").path("dynastyChar"); }

    /* ---- member4 意象分析 ---- */
    /** 意象总次数 [{imagery,count}] */
    public JsonNode imageryTotal() { return root.path("member4").path("total"); }
    /** 意象 TopN [{imagery,count}] */
    public JsonNode imageryTopN() { return root.path("member4").path("topn"); }
    /** 朝代×意象 {dynasty:[{imagery,count}]} */
    public JsonNode imageryByDynasty() { return root.path("member4").path("byDynasty"); }
    /** 作者×意象 Top {author:[{imagery,count}]} */
    public JsonNode imageryByAuthor() { return root.path("member4").path("byAuthorTop"); }

    /** KPI 元信息 */
    public JsonNode meta() { return root.path("meta"); }
}
