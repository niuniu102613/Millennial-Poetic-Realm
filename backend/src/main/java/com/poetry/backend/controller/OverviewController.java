package com.poetry.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.poetry.backend.service.ExtraDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 诗词数据概览 REST 接口（对应 5 号负责的类别/长度/朝代×类别 MR）。
 */
@RestController
@RequestMapping("/api/overview")
@CrossOrigin(origins = "*")
public class OverviewController {

    @Autowired
    private ExtraDataService service;

    /** 1. 各类别数量 */
    @GetMapping("/categoryCount")
    public JsonNode categoryCount() { return service.categoryCount(); }

    /** 2. 长度区间分布 */
    @GetMapping("/lengthRange")
    public JsonNode lengthRange() { return service.lengthRange(); }

    /** 3. 朝代×类别分布 */
    @GetMapping("/dynastyCategory")
    public JsonNode dynastyCategory() { return service.dynastyCategory(); }
}
