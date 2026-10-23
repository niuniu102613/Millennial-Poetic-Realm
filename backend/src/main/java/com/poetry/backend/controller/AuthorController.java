package com.poetry.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.poetry.backend.service.ExtraDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 诗人图谱 REST 接口（对应 2 号负责的作者分析 MR）。
 */
@RestController
@RequestMapping("/api/author")
@CrossOrigin(origins = "*")
public class AuthorController {

    @Autowired
    private ExtraDataService service;

    /** 1. 作者作品数量 TopN */
    @GetMapping("/workTopN")
    public JsonNode workTopN() { return service.authorWorkTopN(); }

    /** 2. 作者平均作品长度 TopN */
    @GetMapping("/avgLenTopN")
    public JsonNode avgLenTopN() { return service.authorAvgLenTopN(); }

    /** 3. 各朝代代表作者 */
    @GetMapping("/dynastyTop")
    public JsonNode dynastyTop() { return service.dynastyTopAuthors(); }
}
