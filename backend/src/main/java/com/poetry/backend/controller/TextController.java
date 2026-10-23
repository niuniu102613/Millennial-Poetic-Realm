package com.poetry.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.poetry.backend.service.TextImageryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 诗词文本分析 REST 接口（对应 3 号负责的高频字/高频词/长度分布/朝代高频字 MR）。
 */
@RestController
@RequestMapping("/api/text")
@CrossOrigin(origins = "*")
public class TextController {

    @Autowired
    private TextImageryDataService service;

    /** 1. 高频字统计 */
    @GetMapping("/charFreq")
    public JsonNode charFreq() { return service.charFreq(); }

    /** 2. 高频词统计 */
    @GetMapping("/wordFreq")
    public JsonNode wordFreq() { return service.wordFreq(); }

    /** 3. 诗词长度分布 */
    @GetMapping("/lengthDist")
    public JsonNode lengthDist() { return service.lengthDist(); }

    /** 4. 各朝代高频字 */
    @GetMapping("/dynastyChar")
    public JsonNode dynastyChar() { return service.dynastyChar(); }
}
