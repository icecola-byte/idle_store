package com.lh.idlestore.commodity.service.impl;


import com.lh.idlestore.commodity.service.CommodityCategoryService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommodityCategoryServiceImplTest {

    @Resource
    private CommodityCategoryService commodityCategoryService;

    @Test
    public void queryEnabledCommodityCategories() {
        commodityCategoryService.getCommodityCategoryTree().forEach(System.out::println);
    }
}