package com.example.audiobackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.audiobackend.service.ChatService;


import java.util.Map;

@RestController
public class AiController {

    @Autowired
    private ChatService chatService;
    
    @PostMapping("/api/ai/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String aiReply = chatService.chat(userMessage);
        return Map.of("reply", aiReply);
    }
}//controller层只负责接受请求，获取用户输入的消息，调用service层的方法处理消息，返回处理结果给前端。真正的业务逻辑（调用AI接口、处理SQL查询等）都在service层实现，controller层保持简洁。
