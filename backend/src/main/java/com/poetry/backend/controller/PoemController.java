package com.poetry.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首页诗笺接口：返回悬挂在古树上的诗词列表。
 * 当前从 home_poems.json 读取，后续可替换为 HBase 查询。
 */
@RestController
@RequestMapping("/api/poem")
@CrossOrigin(origins = "*")
public class PoemController {

    private List<Map<String, Object>> poems = new ArrayList<>();

    @PostConstruct
    public void load() {
        try (InputStream in = getClass().getResourceAsStream("/home_poems.json")) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(in);
            for (JsonNode node : root) {
                poems.add(mapper.convertValue(node, Map.class));
            }
            System.out.println("[PoemController] 首页诗笺加载：" + poems.size() + " 首");
        } catch (Exception e) {
            System.err.println("[PoemController] 加载失败: " + e.getMessage());
        }
    }

    /** 首页全部诗笺 */
    @GetMapping("/home")
    public List<Map<String, Object>> home() {
        return poems;
    }
}
