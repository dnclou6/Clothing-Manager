package com.example.AsmGD1.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔐 Thay bằng API key của bạn
    private final String API_KEY = "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";

    // ✅ Gọi bằng phương thức POST (dùng từ JavaScript fetch hoặc client app)
    @PostMapping("/chat")
    public ResponseEntity<?> chatPost(@RequestBody Map<String, Object> requestBody) {
        return callOpenRouter(requestBody.get("message"));
    }

    // ✅ Gọi bằng phương thức GET (truy cập trực tiếp từ trình duyệt)
    @GetMapping("/chat")
    public ResponseEntity<?> chatGet(@RequestParam("message") String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "Bạn cần gì ở tôi?"));
        messages.add(Map.of("role", "user", "content", userMessage));
        return callOpenRouter(messages);
    }

    // ✅ Hàm xử lý chung gọi OpenRouter
    private ResponseEntity<?> callOpenRouter(Object messages) {
        String apiUrl = "https://openrouter.ai/api/v1/chat/completions";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "openai/gpt-3.5-turbo");
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);
        headers.set("HTTP-Referer", "http://localhost");
        headers.set("X-Title", "java chat client");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map body = response.getBody();

            if (body != null && body.containsKey("choices")) {
                Map choice = (Map) ((List<?>) body.get("choices")).get(0);
                Map message = (Map) choice.get("message");
                return ResponseEntity.ok(Map.of("reply", message.get("content")));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Không có phản hồi từ AI"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
