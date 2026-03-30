package com.example.audiobackend.interceptor;

import com.example.audiobackend.exception.BusinessException;
import com.example.audiobackend.util.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate; // 新增：Redis模板
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器：拦截所有请求，校验 Token（登录之后的事，每一次请求都要校验Token是否有效）
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    // ========== 核心新增1：注入Redis模板 ==========
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Token
        String token = request.getHeader("Authorization");

        // 校验Token是否存在
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未登录，请先登录！");
        }

        // ========== 新增：先去掉Bearer前缀（适配前端传参格式） ==========
        // 原因：前端传的Token格式是 "Bearer xxx"，需要先提取纯Token字符串
        if (token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "").trim();
        }

        // 原有逻辑：校验Token本身是否合法（没过期、签名正确）
        jwtUtil.validateToken(token);

        // ========== 核心新增2：校验Redis中是否存在该Token ==========
        try {
            // 步骤1：解析Token获取用户ID（用你补的getUserIdFromToken方法）
            Long userId = jwtUtil.getUserIdFromToken(token);
            // 步骤2：拼接Redis的Key（和登录接口保持一致：token:用户ID）
            String redisKey = "token:" + userId;
            
            // 步骤3：双重校验Redis中的Token
            // ① 检查Redis中是否有这个Key（用户已退出登录则不存在）
            if (!stringRedisTemplate.hasKey(redisKey)) {
                throw new BusinessException(401, "Token已失效（已退出登录）！");
            }
            // ② 检查Redis中的Token和请求的Token是否一致（防止多端登录旧Token失效）
            String redisToken = stringRedisTemplate.opsForValue().get(redisKey);
            if (!token.equals(redisToken)) {
                throw new BusinessException(401, "Token已失效（账号在其他设备登录）！");
            }
        } catch (BusinessException e) {
            // 自定义异常直接抛出（由全局异常处理器处理）
            throw e;
        } catch (Exception e) {
            // 其他异常（比如解析用户ID失败）
            throw new BusinessException(401, "Token解析失败：" + e.getMessage());
        }

        // 原有逻辑：角色权限控制
        String role = jwtUtil.getRoleFromToken(token);
        if (request.getRequestURI().startsWith("/api/admin/") && !"admin".equals(role)) {
            throw new BusinessException(403, "无管理员权限，禁止访问！");
        }

        // 原有逻辑：放行
        return true;
    }
}