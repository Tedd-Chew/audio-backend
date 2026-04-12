package com.example.audiobackend.config;

import com.example.audiobackend.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web 配置类：注册拦截器 + 解决跨域
 */
@Configuration // ① 标识这是配置类，Spring 启动时加载
public class WebConfig implements WebMvcConfigurer {

    @Resource // ② 注入拦截器对象
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器：配置拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/**") // 拦截所有
            .excludePathPatterns(
                // ====================== 必须放行 ======================
                "/api/user/login",                // 登录

                // 产品列表 + 分页（用户没登录也能看商品）
                "/api/product/list",
                "/api/product/page",
                
                // 商品详情（公开接口）
                "/api/product/*",
                
                // AI 全部放行
                "/api/ai/**",

                // ====================== Swagger 放行 ======================
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
            );
}

    /**
     * 解决跨域问题（前端访问后端必加，否则浏览器报错）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // ⑤ 所有接口允许跨域
                .allowedOriginPatterns("*") // ⑥ 允许所有域名访问
                .allowedMethods("GET", "POST", "PUT", "DELETE") // ⑦ 允许所有请求方法
                .allowedHeaders("*") // ⑧ 允许所有请求头（包括 Authorization）
                .allowCredentials(true); // ⑨ 允许携带 Cookie（可选）
    }
}
