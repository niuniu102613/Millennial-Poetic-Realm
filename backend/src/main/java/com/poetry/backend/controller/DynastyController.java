package com.poetry.backend.controller;

import com.poetry.backend.service.DynastyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 朝代分析 REST 接口（对应 1 号负责的 4 个 MR 结果）。
 */
@RestController
@RequestMapping("/api/dynasty")
@CrossOrigin(origins = "*")   // 允许前端跨域访问
public class DynastyController {

    @Autowired
    private DynastyDataService service;

    /** 1. 各朝代诗词数量 */
    @GetMapping("/poemCount")
    public List<Map<String, Object>> poemCount() {
        return service.getPoemCount();
    }

    /** 2. 各朝代作者数量 */
    @GetMapping("/authorCount")
    public List<Map<String, Object>> authorCount() {
        return service.getAuthorCount();
    }

    /** 3. 各朝代平均诗词长度 */
    @GetMapping("/avgLen")
    public List<Map<String, Object>> avgLen() {
        return service.getAvgLen();
    }

    /** 4. 诗词数量 TopN，例如 /api/dynasty/top/3 */
    @GetMapping("/top/{n}")
    public List<Map<String, Object>> top(@PathVariable int n) {
        return service.getTopN(n);
    }
}
