package com.example.audiobackend.controller;

import com.example.audiobackend.common.Result;
import com.example.audiobackend.entity.SysUser;
import com.example.audiobackend.service.SysUserService;
import com.example.audiobackend.util.JwtUtil;
import com.example.audiobackend.util.PasswordUtil;
import org.springframework.data.redis.core.StringRedisTemplate; // 新增：Redis模板
import org.springframework.web.bind.annotation.*; // 新增：导入RequestHeader

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit; // 新增：时间单位

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private PasswordUtil passwordUtil;

    // ========== 核心新增1：注入Redis模板 ==========
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 登录接口（修改：新增Token存入Redis逻辑）
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody SysUser sysUser) {
        // 1. 原有逻辑：查数据库 + 校验密码（完全保留，不修改）
        //这一步实际上是在用传入的用户名查mysql，看是否有这个用户，然后再比对传入的用户密码和数据库中该用户的密码
        SysUser dbUser = sysUserService.getUserByUsername(sysUser.getUsername());
        if (dbUser == null) {
            return Result.error("用户名或密码错误");
        }
        if (!passwordUtil.match(sysUser.getPassword(), dbUser.getPassword())) {//比对传入的密码和数据库中该用户的密码（加密后）是否匹配
            return Result.error("用户名或密码错误");
        }

        // 2. 原有逻辑：生成Token（完全保留）
        String token = jwtUtil.generateToken(dbUser.getUsername(), dbUser.getId(), dbUser.getRole());

        // ========== 核心新增2：把Token存入Redis ==========
        // Key设计：token:用户ID → 唯一标识该用户的有效Token//token:1
        String redisKey = "token:" + dbUser.getId();//用户名可能可以改，但用户id唯一，所以用id作为Key的一部分更合理；
        // 过期时间：和JWT一致（转毫秒为秒，Redis默认单位是秒）
        long expireSeconds = jwtUtil.getExpireTime() / 1000;
        // 存入Redis：Key + Token值 + 过期时间,这一行真正实现把token存入redis
        stringRedisTemplate.opsForValue().set(redisKey, token, expireSeconds, TimeUnit.SECONDS);//实际上rediskey下面只存了token,时间和单位只是这个key的属性，表示这个key的过期时间

        // 3. 原有逻辑：返回Token（完全保留）
        return Result.success(token);
    }

    // ========== 核心新增3：退出登录接口 ==========
    /**
     * 退出登录接口
     * 作用：删除Redis中的Token，实现Token主动失效
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // 步骤1：提取Token（去掉Bearer 前缀）
            String token = authHeader.replace("Bearer ", "").trim();
            
            // 步骤2：解析Token获取用户ID（用你补的getUserIdFromToken方法）
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 步骤3：删除Redis中的Token（核心：主动失效）
            String redisKey = "token:" + userId;
            stringRedisTemplate.delete(redisKey);//stringRedisTemplate相当于一个遥控器，是我们和redis数据库交互的工具，delete方法就是让redis删除这个key（也就删除了这个用户的token）

            return Result.success("退出登录成功");
        } catch (Exception e) {
            // 异常处理：避免因Token解析失败导致接口报错
            return Result.error("退出登录失败：" + e.getMessage());
        }
    }
}