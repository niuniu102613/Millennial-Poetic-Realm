
package com.poetry.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "千年诗境后端运行成功！");
        result.put("project", "Millennial Poetic Realm");
        result.put("status", "success");
        return result;
    }
}
