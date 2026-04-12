//服务实现类！！！

package com.example.audiobackend.service.impl;

import com.example.audiobackend.entity.Product;
import com.example.audiobackend.mapper.ProductMapper;
import com.example.audiobackend.service.ProductService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
// import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Random;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;



@Service
/* @Service 注解（核心）
作用：告诉 Spring“这个类是服务层组件，我要创建这个类的对象，并放到 Spring 容器中管理”；
运行逻辑：Spring 启动时扫描到@Service → 创建ProductServiceImpl对象 → 后续Controller层用@Autowired就能直接取这个对象用；
对比：和@RestController一样，都是 Spring 的 “组件注解”，只是分工不同（@Service标记服务层，@RestController标记控制器层）*/

// 产品服务实现类：实现ProductService接口，提供所有产品相关的业务逻辑
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private void asyncAddViewCount(Integer productId) {
    // 拼接 Redis 的 key
    String key = "product:viewCount:" + productId;

    // 丢给线程池异步执行 +1
    threadPoolExecutor.execute(() -> {
        stringRedisTemplate.opsForHash().increment(
            key,        // Redis 的 key
            "viewCount",// 字段名
            1           // 每次 +1
        );//原子自增，不会有并发问题
    });
}
    private String getProductKey(Long id) {//用来获得产品的rediskey
        return "product:" + id;
    }


    // 依然加 @Override（因为实现了接口），只改方法内部逻辑
    @Override
    public List<Product> list() {
        return productMapper.selectList(null);
    }

    @Override
    public Page<Product> getProductPage(Integer pageNum, Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        return productMapper.selectPage(page, null);
    }
    @Override
    public Product getById(Long id) {
        //先从redis hash中取数据
        //没有的话再查数据库
        long expireSeconds = 12 * 3600 + new Random().nextInt(30 * 60);//过期时间：12小时 + 0~30分钟的随机数，防止缓存雪崩
        String nullKey ="product:null:"+id;//防止缓存穿透的key,用来缓存空值

        if(stringRedisTemplate.hasKey(nullKey)){
            return null;//如果这个key存在，说明之前查询过数据库没有这个商品，我们直接返回null，不用再查数据库了。
        }
        String redisKey = getProductKey(id);
        Map<Object, Object> hashData = stringRedisTemplate.opsForHash().entries(redisKey);
        if(!hashData.isEmpty()){
            Product product = new Product();
            product.setId(id);
            product.setName((String) hashData.get("name"));
            product.setPrice(Double.parseDouble((String) hashData.get("price")));
            product.setDescription((String) hashData.get("description"));
            product.setImageUrl((String) hashData.get("imageUrl"));
            return product;
        }
        //缓存没命中，查数据库
        String lockKey = "lock:product:" + id;
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);

        //------------------------------------------
        //用分布式锁解决缓存击穿问题（热点数据过期时大量请求直接打到数据库上）
        //------------------------------------------
        // 没抢到锁 → 重试
        if (lock == null || !lock) {
            try {
                Thread.sleep(50); // 休息一下
            } catch (InterruptedException e) {}
            return getById(id); // 重新调用，直接拿缓存
        }
        try {
            // 抢到锁 → 查数据库 + 写缓存
            Product product = productMapper.selectById(id);
            if(product != null){
                Map<String, String> map = new HashMap<>();
                map.put("id", product.getId().toString());
                map.put("name", product.getName());
                map.put("price", product.getPrice().toString());
                map.put("description", product.getDescription());
                map.put("imageUrl", product.getImageUrl());
                map.put("viewCount", product.getViewCount().toString());
                stringRedisTemplate.opsForHash().putAll(redisKey, map);
                stringRedisTemplate.expire(redisKey,expireSeconds, TimeUnit.SECONDS);
            } else {
                stringRedisTemplate.opsForValue().setIfAbsent(
                    nullKey, "true", 1, TimeUnit.MINUTES
                );
                return null;
            }
            // 查数据库 + 写缓存完成后，异步增加 viewCount
            asyncAddViewCount(id.intValue());
            return product;
        } finally {
           // 释放锁
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(Product product) {
        
        boolean success = productMapper.insert(product) > 0;//影响行数大于0，说明新增成功，否则失败。
        if (success) {
            // 刚插入，原来的“不存在”缓存失效了
            String nullKey = "product:null:" + product.getId();
            stringRedisTemplate.delete(nullKey);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Product product) {
        
        boolean success = productMapper.updateById(product) > 0;
        if (success) {
            // 删除缓存，保证一致性，如果不删除，缓存的数据会和数据库不一致，用户拿到的就是旧数据了。
            String redisKey = "product:" + product.getId();
            stringRedisTemplate.delete(redisKey);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        boolean success = productMapper.deleteById(id) > 0;
        if (success) {
            // 删除正常缓存
            String redisKey = "product:" + id;
            stringRedisTemplate.delete(redisKey);
        }
        return success;
    }
}