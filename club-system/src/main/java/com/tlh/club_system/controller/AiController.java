package com.tlh.club_system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 辅助功能控制器
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String apiModel;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiController() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * AI 润色活动内容
     * @param params { "title": "...", "content": "..." }
     */
    @PostMapping("/polish")
    public Result<String> polishContent(@RequestBody Map<String, String> params) {
        UserContext.getCurrentUser(); // 确保已登录（可选）

        String title = params.getOrDefault("title", "未命名活动");
        String content = params.getOrDefault("content", "");

        if (content.trim().isEmpty()) {
            return Result.error("请先填写一些简单的活动描述");
        }

        try {
            // 构造 Prompt
            String systemPrompt = "你是一个高校社团管理系统的智能助手。请帮我润色以下社团活动的描述，使其更加吸引人、专业且充满活力。请直接返回润色后的内容，不要包含任何客套话或额外说明。";
            String userPrompt = String.format("活动主题：%s\n\n草稿内容：%s", title, content);

            // 构造 OpenAI 格式请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", apiModel);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.7);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 发送请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // 解析响应
                JsonNode rootNode = objectMapper.readTree(response.body());
                String polishedText = rootNode.path("choices").get(0).path("message").path("content").asText();
                return Result.success("AI 润色成功", polishedText);
            } else {
                System.err.println("AI API Error: " + response.body());
                return Result.error("AI 服务响应异常: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 服务调用失败: " + e.getMessage());
        }
    }
}
