package com.millennial.member4.backend.imagery.controller;

import com.millennial.member4.backend.common.Result;
import com.millennial.member4.backend.imagery.service.ImageryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imagery")
public class ImageryController {

    @Resource
    private ImageryService imageryService;

    /**
     * 接口1：所有意象总次数 → 前端词云图
     */
    @GetMapping("/total")
    public Result<Map<String, Integer>> getImageryTotal() {
        try {
            return Result.success(imageryService.getImageryTotal());
        } catch (IOException e) {
            return Result.error("获取意象总次数失败：" + e.getMessage());
        }
    }

    /**
     * 接口2：意象TopN排行榜 → 前端柱状图
     */
    @GetMapping("/topn")
    public Result<List<Map<String, Object>>> getImageryTopN(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return Result.success(imageryService.getImageryTopN(limit));
        } catch (IOException e) {
            return Result.error("获取意象TopN失败：" + e.getMessage());
        }
    }

    /**
     * 接口3：分朝代意象分布 → 前端对比图
     */
    @GetMapping("/by-dynasty")
    public Result<Map<String, Integer>> getImageryByDynasty(
            @RequestParam(required = false) String dynasty) {
        try {
            return Result.success(imageryService.getImageryByDynasty(dynasty));
        } catch (IOException e) {
            return Result.error("获取朝代意象分布失败：" + e.getMessage());
        }
    }

    /**
     * 接口4：分作者意象偏好 → 前端诗人分析
     */
    @GetMapping("/by-author")
    public Result<Map<String, Integer>> getImageryByAuthor(
            @RequestParam(required = false) String author) {
        try {
            return Result.success(imageryService.getImageryByAuthor(author));
        } catch (IOException e) {
            return Result.error("获取作者意象偏好失败：" + e.getMessage());
        }
    }
}
