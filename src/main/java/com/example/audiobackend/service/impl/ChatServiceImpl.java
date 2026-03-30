package com.example.audiobackend.service.impl;

import com.example.audiobackend.service.ChatService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.audiobackend.util.AiChatUtil;


@Service
public class ChatServiceImpl implements ChatService {

    // 这里注入工具
    @Autowired
    private AiChatUtil aiChatUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;//JdbcTemplate 就是 Spring 提供的、用来执行 SQL 语句的工具！

    @Override
    public String chat(String userMessage) {

        String aiReply = aiChatUtil.chat(userMessage);
        aiReply = aiReply.trim();//去掉首尾空格，防止后面判断出问题

        if (aiReply.startsWith("NEED_SQL:")) {
            String sql = aiReply.substring("NEED_SQL:".length()).trim();
            // 简单安全校验：只允许查询语句
            String lowerSql = sql.toLowerCase().trim();//变成小写，去掉首尾空格，方便后续判断开头
            if (!lowerSql.startsWith("select")) {
                return "抱歉，目前只支持数据查询哦";
            }
            try {
                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                String ragPrompt = """
                用户的问题：%s

                数据库查询到的真实数据：
                %s

                请你严格遵守以下规则：
                1. 只根据上面的真实数据回答，不许编造信息
                2. 用通顺、自然的中文回答
                3. 不要使用 JSON、代码块、表格
                4. 回答完整，不要中途截断
                5. 语言简洁专业，不要多余表情和符号
                """.formatted(userMessage, result.toString());
                        //再一次调用ai模型，传入数据库返回信息，让ai用人话回答
                return aiChatUtil.chat(ragPrompt);
            } catch (Exception e) {
                //return "查询失败：" + e.getMessage();//不能把系统内部的错误信息丢给用户，改成更友好的提示
                return "抱歉，查询失败了，请检查问题或稍后重试。";
            }
        }
        return aiReply;
    }
}
