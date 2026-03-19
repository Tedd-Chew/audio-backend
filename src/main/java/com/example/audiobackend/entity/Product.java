package com.example.audiobackend.entity;
// Lombok注解：自动生成getter/setter/无参构造/全参构造（不用手写几百行重复代码）
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data  // @Data：替代所有getter/setter/toString等方法，企业必用
@NoArgsConstructor// 无参构造器，Lombok会自动生成
@AllArgsConstructor// 全参构造器，Lombok会自动生成
public class Product {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String imageUrl;// 注意：Java 用驼峰，MyBatis-Plus 会自动映射到数据库的 image_url
}