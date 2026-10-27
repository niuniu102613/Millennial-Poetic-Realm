package com.poetry.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.poetry.backend.service.TextImageryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 诗境意象 REST 接口（对应 4 号负责的意象次数/TopN/朝代×意象/作者×意象 MR）。
 */
@RestController
@RequestMapping("/api/imagery")
@CrossOrigin(origins = "*")
public class ImageryController {

    @Autowired
    private TextImageryDataService service;

    /** 1. 意象出现次数统计（全量） */
    @GetMapping("/total")
    public JsonNode total() { return service.imageryTotal(); }

    /** 2. 意象 TopN 排行 */
    @GetMapping("/topn")
    public JsonNode topn() { return service.imageryTopN(); }

    /** 3. 朝代 × 意象 */
    @GetMapping("/by-dynasty")
    public JsonNode byDynasty() { return service.imageryByDynasty(); }

    /** 4. 作者 × 意象（Top 作者） */
    @GetMapping("/by-author")
    public JsonNode byAuthor() { return service.imageryByAuthor(); }

    /** KPI 元信息 */
    @GetMapping("/meta")
    public JsonNode meta() { return service.meta(); }

    /** 5. 意象共现热力图（按朝代筛选） */
    @GetMapping("/correlation")
    public Map<String, Object> correlation(@RequestParam(required = false, defaultValue = "") String dynasty) {
        return service.correlation(dynasty);
    }

    /** 6. 诗人 × 意象 诗句查询 */
    @GetMapping("/poetSentence")
    public Map<String, String> poetSentence(@RequestParam String poet, @RequestParam String imagery) {
        return service.poetSentence(poet, imagery);
    }
}
