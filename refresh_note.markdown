# Spring Boot 音响产品后端项目笔记（按项目流程整理）
## 一、项目整体架构与核心流程
### 1. 项目目录结构
| 包/文件路径                          | 核心作用                                                                 | 对应类示例                |
|-------------------------------------|--------------------------------------------------------------------------|---------------------------|
| `com.example.audiobackend.AudiobackendApplication.java` | 项目启动类（总开关），触发Spring初始化、Tomcat启动、组件扫描              | 启动类本身                |
| `com.example.audiobackend.controller` | 对接前端，接收请求/返回数据，不写业务逻辑                                | `ProductController`、`UserController` |
| `com.example.audiobackend.service`   | 核心业务逻辑层，处理增删改查、缓存、权限校验等                            | `ProductService`（接口）  |
| `com.example.audiobackend.service.impl` | Service接口实现类，落地具体业务逻辑                                      | `ProductServiceImpl`      |
| `com.example.audiobackend.entity`     | 实体类，描述数据结构（对应数据库表）                                      | `Product`                 |
| `com.example.audiobackend.common`     | 通用工具类，如统一返回结果、全局异常处理、JWT工具                        | `Result`、`GlobalExceptionHandler`、`JwtUtil` |
| `com.example.audiobackend.config`     | 配置类，定义Web、Redis、MyBatis等规则                                    | `WebConfig`（拦截器配置）、`RedisConfig` |
| `com.example.audiobackend.mapper`     | 数据访问层，对接MySQL，执行SQL                                            | `ProductMapper`           |

### 2. 核心请求流程
```
前端请求 → Controller层（接收请求）→ @Autowired注入Service对象 → Service层（处理业务逻辑）→ Mapper层（操作数据库/Redis）→ 结果逐层返回 → Controller封装Result → 前端
```

## 二、启动类与核心注解（AudiobackendApplication）
### 1. 核心注解：@SpringBootApplication
| 拆解注解         | 作用                                                                 |
|------------------|----------------------------------------------------------------------|
| `@Configuration` | 标记为配置类，允许自定义配置（如手动注册Bean）                        |
| `@EnableAutoConfiguration` | 自动配置Spring Boot（Tomcat端口、JSON解析、数据库连接等）           |
| `@ComponentScan` | 扫描当前包及子包下的@Component/@Service/@Controller等组件，创建对象并放入Spring容器 |

### 2. 启动执行逻辑
```java
@SpringBootApplication
public class AudiobackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AudiobackendApplication.class, args);
        // 执行逻辑：加载自动配置类 → 启动Tomcat（绑定8080端口）→ 扫描组件 → 创建Spring容器 → 监听请求
    }
}
```

## 三、实体类设计（entity/Product）
### 1. Lombok核心注解（标配）
| 注解                | 生成内容                          | 使用者       | 核心用途                                                                 |
|---------------------|-----------------------------------|--------------|--------------------------------------------------------------------------|
| `@Data`             | getter/setter、toString、equals等 | 开发者+Spring | 开发者：快速操作属性；Spring：通过setter给对象赋值                        |
| `@NoArgsConstructor` | 无参构造方法                      | Spring       | Spring创建对象的入口，无此注解项目启动失败                                |
| `@AllArgsConstructor` | 全参构造方法                      | 开发者       | 快速创建含完整属性的对象（如`new Product(1L, "蓝牙音箱", 199.99, ...)`） |

### 2. 标准实体类代码
```java
package com.example.audiobackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;          // 产品ID（对应数据库主键）
    private String name;      // 产品名称（映射数据库name字段）
    private Double price;     // 产品价格（映射数据库price字段）
    private String description; // 产品描述
    private String imageUrl;  // 产品图片路径（自动映射数据库image_url字段）
}
```

## 四、数据访问层（mapper/ProductMapper）
### 1. 核心配置（application.properties）
```properties
# MySQL连接配置
spring.datasource.url=jdbc:mysql://localhost:3306/audio_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=Zou1218yu
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis配置
mybatis-plus.mapper-locations=classpath:mapper/*.xml
mybatis-plus.type-aliases-package=com.example.audiobackend.entity
```

### 2. Mapper接口与注解
| 注解         | 作用                                                                 | 示例                          |
|--------------|----------------------------------------------------------------------|-------------------------------|
| `@Mapper`    | MyBatis生成代理对象，让接口可执行SQL（核心）                          | `@Mapper public interface ProductMapper {}` |
| `@Repository` | 语义标记（数据层），消除IDE警告（非必需，可通过@MapperScan批量扫描） | 同上                          |

### 3. 核心CRUD（MyBatis手动SQL示例）
```java
public interface ProductMapper {
    // 查询所有产品
    @Select("SELECT * FROM product")
    List<Product> selectAll();
    
    // 新增产品
    @Insert("INSERT INTO product(name, price, description, imageUrl) VALUES(#{name}, #{price}, #{description}, #{imageUrl})")
    int insert(Product product);
    
    // 修改产品
    @Update("UPDATE product SET name=#{name}, price=#{price} WHERE id=#{id}")
    int updateById(Product product);
    
    // 删除产品
    @Delete("DELETE FROM product WHERE id=#{id}")
    int deleteById(Long id);
}
```

## 五、服务层（service/ProductService + impl/ProductServiceImpl）
### 1. 核心注解与逻辑
| 注解         | 作用                                                                 | 示例位置                      |
|--------------|----------------------------------------------------------------------|-------------------------------|
| `@Service`   | 标记为服务层组件，Spring创建对象并放入容器                           | `ProductServiceImpl`类上      |
| `@Autowired` | 从Spring容器注入Mapper/RedisTemplate等对象（按类型匹配）             | ServiceImpl内注入ProductMapper |
| `@Resource`  | 从Spring容器注入对象（先按名称，再按类型，JDK自带）                  | 替代@Autowired，如注入RedisTemplate |

### 2. 业务逻辑示例（整合MySQL + Redis缓存）
```java
// Service接口
public interface ProductService {
    List<Product> list();
}

// Service实现类
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<Product> list() {
        // 1. 先查Redis缓存
        String cacheKey = "product_list";
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存存在，直接返回
            return JSON.parseArray(cacheValue, Product.class);
        }
        
        // 2. 缓存不存在，查MySQL
        List<Product> productList = productMapper.selectAll();
        
        // 3. 存入Redis，设置10分钟过期
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(productList), 10, TimeUnit.MINUTES);
        
        return productList;
    }
}
```

## 六、控制器层（controller/ProductController）
### 1. 核心注解
| 注解                | 作用                                                                 | 示例                          |
|---------------------|----------------------------------------------------------------------|-------------------------------|
| `@RestController`   | 标记为控制器，返回JSON数据（@Controller + @ResponseBody）            | 类上                          |
| `@RequestMapping`   | 定义父路径（如/api/product）                                         | 类上：`@RequestMapping("/api/product")` |
| `@GetMapping`/`@PostMapping` | 定义子路径+请求方式（如/list、/add）                               | 方法上：`@GetMapping("/list")` |
| `@Autowired`        | 注入Service对象（无需手动new）                                       | 注入ProductService            |

### 2. 接口示例（统一返回Result）
```java
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    // 查询产品列表
    @GetMapping("/list")
    public Result list() {
        List<Product> productList = productService.list();
        return Result.success(productList); // 统一返回格式：{code:200, msg:"成功", data:[]}
    }
    
    // 新增产品
    @PostMapping("/add")
    public Result add(@RequestBody Product product) {
        int count = productService.add(product);
        return count > 0 ? Result.success() : Result.error("新增失败");
    }
}
```

### 3. 统一返回结果（common/Result）
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code; // 200成功，401未登录，500异常
    private String msg;
    private Object data;

    // 静态工具方法
    public static Result success() {
        return new Result(200, "操作成功", null);
    }
    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }
    public static Result error(String msg) {
        return new Result(500, msg, null);
    }
    public static Result unauthorized() {
        return new Result(401, "未登录或Token失效", null);
    }
}
```

## 七、登录认证（JWT + 拦截器）
### 1. JWT核心工具类（common/JwtUtil）
```java
public class JwtUtil {
    // 密钥（自定义，需保密）
    private static final String SECRET_KEY = "audio_backend_2024";
    // 过期时间（2小时）
    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000L;

    // 生成Token
    public static String generateToken(String username) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        return Jwts.builder()
                .setSubject(username) // 载荷：存用户名
                .setExpiration(expireDate) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名
                .compact();
    }

    // 校验Token
    public static boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 从Token获取用户名
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
```

### 2. 登录接口（UserController）
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    // 模拟用户数据（实际查数据库，密码用BCrypt加密）
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "$2a$10$xxxxxx"; // 123456的BCrypt密文

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 校验用户名+密码（实际查数据库）
        if (USERNAME.equals(user.getUsername()) && BCrypt.checkpw(user.getPassword(), PASSWORD)) {
            // 生成Token
            String token = JwtUtil.generateToken(user.getUsername());
            return Result.success(token);
        }
        return Result.error("用户名或密码错误");
    }
}
```

### 3. 拦截器（config/JwtInterceptor + WebConfig）
```java
// 拦截器类（@Component标记为Spring组件）
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的Token
        String token = request.getHeader("Authorization");
        // 2. 校验Token
        if (token == null || !JwtUtil.validateToken(token)) {
            // 3. 校验失败，返回401
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSON.toJSONString(Result.unauthorized()));
            return false;
        }
        // 4. 校验通过，放行
        return true;
    }
}

// Web配置类（@Configuration标记为配置类，执行配置方法）
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource // 按名称注入拦截器对象
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器，拦截/api/**请求（排除登录接口）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login");
    }

    // 跨域配置（可选）
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## 八、全局异常处理（common/GlobalExceptionHandler）
### 1. 核心注解
| 注解                  | 作用                                                                 |
|-----------------------|----------------------------------------------------------------------|
| `@RestControllerAdvice` | 全局拦截Controller异常，自动返回JSON（@ControllerAdvice + @ResponseBody） |
| `@ExceptionHandler`   | 指定处理的异常类型（如Token过期、空指针、参数错误）                   |

### 2. 实现代码
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 处理Token相关异常
    @ExceptionHandler(ExpiredJwtException.class)
    public Result handleTokenExpired(ExpiredJwtException e) {
        log.error("Token过期：", e); // SLF4J日志记录
        return Result.error("Token已过期，请重新登录");
    }

    // 处理空指针异常
    @ExceptionHandler(NullPointerException.class)
    public Result handleNPE(NullPointerException e) {
        log.error("空指针异常：", e);
        return Result.error("系统繁忙，请稍后重试");
    }

    // 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public Result handleAll(Exception e) {
        log.error("系统异常：", e);
        return Result.error("服务器内部错误");
    }
}
```

## 九、核心注解对比表（易混淆注解）
| 注解                | 类型       | 核心作用                                                                 | 适用场景                          |
|---------------------|------------|--------------------------------------------------------------------------|-----------------------------------|
| `@Component`        | 组件标记   | 标记普通类，Spring创建对象并存入容器                                     | 自定义工具类、拦截器等            |
| `@Configuration`    | 配置标记   | 继承@Component，额外执行配置方法（如addInterceptors）                    | WebConfig、RedisConfig等配置类    |
| `@Service`/`@Controller` | 组件标记（@Component衍生） | 语义化标记服务层/控制层，Spring创建对象                                  | ServiceImpl、Controller类         |
| `@Autowired`        | 依赖注入   | Spring专属，按类型匹配注入对象，可配合@Qualifier指定名称                 | 大部分注入场景（一个类型一个对象） |
| `@Resource`         | 依赖注入   | JDK自带，先按名称匹配，再按类型匹配，支持指定name                       | 一个类型多个对象时精准注入        |
| `@Mapper`           | 数据层     | MyBatis生成代理对象，让接口执行SQL                                       | Mapper接口                        |
| `@Repository`       | 数据层     | 语义标记，消除IDE警告（可通过@MapperScan批量替代）                      | Mapper接口                        |

## 十、Redis集成（config/RedisConfig）
### 1. 核心依赖
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId> <!-- 连接池，提升性能 -->
</dependency>
```

### 2. 核心配置（application.properties）
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password= # 无密码则留空
spring.redis.database=0 # 默认使用0号库
spring.redis.timeout=10000ms
# 连接池配置
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0
```

### 3. 核心操作（StringRedisTemplate）
| 方法                          | 作用                     | 示例                                  |
|-------------------------------|--------------------------|---------------------------------------|
| `opsForValue().get(key)`      | 获取缓存值               | `redisTemplate.opsForValue().get("product_list")` |
| `opsForValue().set(key, value, expire, unit)` | 存入缓存并设置过期时间 | 见ProductServiceImpl示例              |
| `opsForValue().delete(key)`   | 删除缓存                 | `redisTemplate.delete("product_list")` |

## 十一、项目优化（提升含金量）
1. **接口文档**：集成Knife4j（基于Swagger），自动生成接口文档，支持在线调试；
2. **密码安全**：使用BCrypt加密存储密码，前端传明文，后端用`BCrypt.hashpw()`加密、`BCrypt.checkpw()`校验；
3. **分页查询**：在ProductService中实现分页，如`/api/product/list?page=1&size=10`；
4. **日志规范**：使用SLF4J + Logback记录日志，区分info/error级别，输出到文件；
5. **事务管理**：在Service层添加`@Transactional`注解，保证增删改的原子性。



`@RestControllerAdvice` 是 SpringBoot 中处理**全局异常**的「核心注解」，你可以把它理解成项目的「统一异常急救中心」—— 不管哪个 Controller 抛出异常，它都会自动拦截并按你设定的规则处理，核心作用是**统一异常返回格式、简化异常处理代码、提升接口健壮性**。

咱们结合项目代码和实际场景，把它的作用、原理、使用方式讲透：

### 一、先懂：为什么需要 `@RestControllerAdvice`？
没有它的话，项目会出现两个致命问题：
1. **异常返回格式混乱**：比如空指针异常返回Tomcat的500错误页面，Token过期返回JWT的原生报错，前端需要适配多种错误格式，开发成本高；
2. **异常代码冗余**：每个Controller的每个方法都要写 `try-catch`，比如：
   ```java
   @GetMapping("/list")
   public Result list() {
       try {
           return Result.success(productService.list());
       } catch (Exception e) {
           return Result.error("查询失败");
       }
   }
   ```
   几十个接口就要写几十遍 `try-catch`，代码又乱又难维护。

而 `@RestControllerAdvice` 能一次性解决这些问题：**全局拦截所有Controller的异常，统一处理、统一返回格式，不用写重复的try-catch**。

### 二、`@RestControllerAdvice` 的核心作用（拆解）
| 核心作用                | 具体效果                                                                 | 项目中的体现                          |
|-------------------------|--------------------------------------------------------------------------|---------------------------------------|
| 1. 全局拦截异常         | 拦截所有 `@RestController`/`@Controller` 抛出的异常（包括自定义异常、系统异常） | 拦截Token过期（`ExpiredJwtException`）、空指针（`NullPointerException`）等 |
| 2. 统一返回格式         | 不管什么异常，都返回 `Result` 格式（`{code:xxx, msg:xxx, data:null}`），前端只需解析一种格式 | 所有异常都返回 `Result.error("xxx")`，而非原生报错 |
| 3. 解耦异常处理逻辑     | 异常处理代码集中在一个类里，Controller 只关注业务逻辑，不用写 try-catch    | ProductController、UserController 都不用处理异常 |
| 4. 自定义异常提示       | 可以给不同异常设置不同的友好提示（比如Token过期提示“请重新登录”，而非技术报错） | 见项目中 `handleTokenExpired` 方法    |

### 三、`@RestControllerAdvice` 的本质（和其他注解的关联）
它是 `@ControllerAdvice` + `@ResponseBody` 的「组合注解」，拆解来看：
| 拆解注解         | 作用                                                                 |
|------------------|----------------------------------------------------------------------|
| `@ControllerAdvice` | 标记一个类为「全局Controller增强类」，能拦截所有Controller的异常/参数等 |
| `@ResponseBody`   | 让方法返回的对象（如Result）自动转为JSON格式（和@RestController里的@ResponseBody作用一致） |

简单说：`@RestControllerAdvice = 全局拦截Controller异常 + 返回JSON格式结果`。

### 四、结合项目代码理解使用方式
项目中你写的 `GlobalExceptionHandler` 类就是典型用法，核心逻辑分两步：
#### 1. 标记类为全局异常处理器
```java
@RestControllerAdvice // 核心注解：全局拦截+返回JSON
public class GlobalExceptionHandler {
    // 异常处理方法...
}
```
这行注解告诉 Spring：「这个类是处理所有 Controller 异常的，处理完要返回 JSON」。

#### 2. 定义具体异常的处理方法（@ExceptionHandler）
`@ExceptionHandler` 是「异常匹配器」，指定该方法处理哪种异常：
```java
// 处理Token过期异常
@ExceptionHandler(ExpiredJwtException.class) // 匹配Token过期异常
public Result handleTokenExpired(ExpiredJwtException e) {
    log.error("Token过期：", e); // 记录日志（排查问题用）
    return Result.error("Token已过期，请重新登录"); // 统一返回Result格式
}

// 处理所有未捕获的异常（兜底）
@ExceptionHandler(Exception.class) // 匹配所有异常（父类）
public Result handleAll(Exception e) {
    log.error("系统异常：", e);
    return Result.error("服务器内部错误");
}
```
#### 执行流程（以Token过期为例）：
1. 用户携带过期Token访问 `/api/product/list`；
2. JwtInterceptor校验Token时抛出 `ExpiredJwtException`；
3. `@RestControllerAdvice` 拦截这个异常，找到 `handleTokenExpired` 方法；
4. 该方法记录日志，返回 `Result.error("Token已过期，请重新登录")`；
5. 前端收到统一的JSON格式：`{code:500, msg:"Token已过期，请重新登录", data:null}`。

### 五、关键细节（避坑点）
1. **异常匹配优先级**：精准异常 > 父类异常（比如先匹配 `ExpiredJwtException`，再匹配 `Exception`）；
2. **只拦截Controller层异常**：Service/Mapper层的异常会向上抛到Controller层，才会被拦截；
3. **和 `@ControllerAdvice` 的区别**：
   - `@ControllerAdvice`：处理异常后返回视图（如error.html），适合传统MVC项目；
   - `@RestControllerAdvice`：处理异常后返回JSON，适合前后端分离项目（咱们的项目就是）；
4. **日志必须加**：处理异常时一定要用 `log.error()` 记录异常栈，否则线上出问题无法排查。

### 核心总结
`@RestControllerAdvice` 的核心价值：
1. **统一异常返回**：所有异常都返回 `Result` 格式，前端不用适配多种错误；
2. **简化代码**：Controller 不用写 try-catch，异常处理逻辑集中管理；
3. **友好提示**：给用户返回易懂的提示（如“Token过期”），而非技术报错（如“ExpiredJwtException”）。

它是前后端分离项目的「标配注解」，能极大提升接口的健壮性和可维护性——比如你后续想加「参数校验异常」处理，只需在 `GlobalExceptionHandler` 里加一个方法即可，不用改任何Controller代码。