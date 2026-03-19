//服务实现类！！！

package com.example.audiobackend.service.impl;

import com.example.audiobackend.entity.Product;
import com.example.audiobackend.mapper.ProductMapper;
import com.example.audiobackend.service.ProductService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import java.util.ArrayList;
import java.util.List;


@Service
/* @Service 注解（核心）
作用：告诉 Spring“这个类是服务层组件，我要创建这个类的对象，并放到 Spring 容器中管理”；
运行逻辑：Spring 启动时扫描到@Service → 创建ProductServiceImpl对象 → 后续Controller层用@Autowired就能直接取这个对象用；
对比：和@RestController一样，都是 Spring 的 “组件注解”，只是分工不同（@Service标记服务层，@RestController标记控制器层）*/

// 产品服务实现类：实现ProductService接口，提供所有产品相关的业务逻辑
public class ProductServiceImpl implements ProductService {

    // // 模拟数据库：用静态List存储产品数据（后续替换为MySQL）
    // // static：项目启动时初始化，全局唯一；final：不可修改List对象本身（但可以增删元素）
    // private static final List<Product> productList = new ArrayList<>();

    // // 静态代码块：项目启动时初始化模拟数据（只执行一次）
    // static {
    //     // 用@AllArgsConstructor生成的全参构造快速创建对象
    //     productList.add(new Product(1L, "家庭影院音响", 1999.99, "5.1声道", "/img/1.jpg"));
    //     productList.add(new Product(2L, "桌面蓝牙音响", 299.99, "无线续航", "/img/2.jpg"));
    //     productList.add(new Product(3L, "车载音响", 899.99, "无损音质", "/img/3.jpg"));
    // }

    @Autowired
    private ProductMapper productMapper;

    // 依然加 @Override（因为实现了接口），只改方法内部逻辑
    @Override
    public List<Product> list() {
        // 把 return productList 改成查询数据库
        return productMapper.selectList(null);
    }

    @Override
    public Product getById(Long id) {
        // 替换原来的流式查询，改成查数据库
        return productMapper.selectById(id);
    }

    @Override
    public boolean add(Product product) {
        // 替换原来的 list.add()，改成新增到数据库
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean update(Product product) {
        // 替换原来的「删了再加」，改成真正的更新数据库
        return productMapper.updateById(product) > 0;
    }

    @Override
    public boolean delete(Long id) {
        // 替换原来的 list.removeIf()，改成删除数据库数据
        return productMapper.deleteById(id) > 0;
    }
}