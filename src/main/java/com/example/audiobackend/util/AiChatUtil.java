package com.example.audiobackend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;//用来发送Http请求和接收响应的工具
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiChatUtil {
    //spring会自动找到默认的配置文件注入配置
    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.api-key}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();//Spring自带的发送Http请求的工具
    private final ObjectMapper objectMapper = new ObjectMapper();//用来解析Json的工具

    public String chat(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = """
                你是智能助手。
                如果用户的问题需要查询数据库，请严格按照格式输出：
                NEED_SQL:生成的MySQL语句
                例如：NEED_SQL:SELECT * FROM sys_user

                如果不需要查数据库也就是用户问题中不包含询问用户或产品有关的问题，直接正常聊天回答。

                数据库表结构如下：
                1. sys_user（系统用户表）
                字段：id（主键，bigint）、username（唯一，varchar(50)）、password（varchar(100)）、role（varchar(20)）
                2. product（商品表）
                字段：id（主键，bigint）、name（varchar(100)）、price（double）、description（varchar(500)，可空）、image_url（varchar(200)，可空）
                禁止说“无法回答”“不能查询”之类的话，禁止重复固定句式，禁止复读。
                只按要求格式输出，不要解释，不要废话。
                用户问题：%s
                """.formatted(prompt);//?这里的%s会被用户输入的问题替换，形成一个完整的系统提示，告诉AI如何回答用户的问题；

            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", systemPrompt);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", List.of(textContent));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(userMessage));
            //上面三坨互相嵌套，最后组成一个Map，作为请求体发送给AI接口，避免人为使用string拼接Json导致格式错误；

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);//这里的request Body是泛型，可以传很多种数据，比如map，user，spring可以把它转成Json字符串发给AI接口；
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, request, String.class
            );//这段才是真正给ai发送消息的方法

            JsonNode root = objectMapper.readTree(response.getBody());//解析Json响应，拿回答；
            return root.get("choices").get(0)
                      .get("message")
                      .get("content")
                      .asText();//从Json中提取回答，返回给调用者；

        } catch (Exception e) {
            e.printStackTrace();
            return "AI错误：" + e.getMessage();
        }
    }
}