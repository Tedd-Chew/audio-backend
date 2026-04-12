package com.example.audiobackend.service.impl;

import com.example.audiobackend.service.ChatService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.audiobackend.util.AiChatUtil;
import java.util.concurrent.ThreadPoolExecutor;


@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private AiChatUtil aiChatUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 注入你自己的线程池
    @Autowired
    private ThreadPoolExecutor executor;

    // ================== 接口方法，返回 String ==================
    @Override
    public String chat(String userMessage) {

        // ==============================================
        // 【第一步：把耗时任务丢给线程池 → 异步执行】
        // ==============================================
        executor.submit(() -> {
            try {
                // ============== 这里是后台执行的真正AI逻辑 ==============
                System.out.println("【异步任务】开始执行AI问答，线程：" + Thread.currentThread().getName());

                // 1. 第一次调用AI：判断是否需要SQL
                String prompt = buildPrompt(userMessage);
                String aiReply = aiChatUtil.sendToAi(prompt).trim();

                // 2. 如果需要SQL
                if (aiReply.startsWith("NEED_SQL:")) {
                    String sql = aiReply.replace("NEED_SQL:", "").trim();

                    // 安全校验
                    if (!sql.toLowerCase().startsWith("select")) {
                        System.out.println("拦截非法SQL");
                        return;
                    }

                    // 3. 查询数据库
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

                    // 4. 第二次调用AI，生成自然语言回答
                    String finalAnswer = aiChatUtil.sendToAi(buildFinalPrompt(userMessage, result));

                    // ==========================================
                    // 【最终结果】这里就是AI的最终回答！
                    // 你可以存Redis、存数据库、发消息队列...
                    // ==========================================
                    System.out.println("【AI最终回答】：" + finalAnswer);

                } else {
                    // 不需要查库，直接输出回答
                    System.out.println("【AI普通回答】：" + aiReply);
                }

            } catch (Exception e) {
                System.out.println("异步任务出错：" + e.getMessage());
            }
        });

        // ==============================================
        // 【第二步：主线程立刻返回！不等待！】
        // ==============================================
        return "AI 正在思考中，请稍等...";
    }

    // ================== 下面是抽取的提示语方法，保持整洁 ==================
    private String buildPrompt(String userMessage) {
        return """
            你是智能助手。
            如果需要查询数据库，输出 NEED_SQL:xxx
            表：sys_user、product
            不需要就直接回答。
            用户问题：%s
            """.formatted(userMessage);
    }

    private String buildFinalPrompt(String userMessage, List<Map<String, Object>> result) {
        return """
            用户问题：%s
            数据：%s
            规则：只根据数据回答，自然中文，简洁专业
            """.formatted(userMessage, result);
    }
}


