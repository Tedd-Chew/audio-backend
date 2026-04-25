package com.example.audiobackend.controller;

import com.example.audiobackend.common.Result;
import com.example.audiobackend.entity.Product;
import com.example.audiobackend.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

import org.springframework.web.bind.annotation.*;





@RestController
@RequestMapping("/api/product")


public class ProductController {


    private final ProductService productService;//声明一个变量，用于存储自动注入的ProductServiceImpl对象（实现了ProductService接口）；

    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @GetMapping("/page")
    public Result<Page<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "3") Integer pageSize
    ) {
        return Result.success(productService.getProductPage(pageNum, pageSize));
    }
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Product product) {
        productService.add(product);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
    public ProductController(ProductService productService)
    {
        this.productService = productService;
    }

}