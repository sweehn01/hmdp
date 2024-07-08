package com.hmdp.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> res = new ArrayList<>();
        // 1. 查询缓存是否存在
        List<String> shopList = redisTemplate
                .opsForList()
                .range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
        // 1.1 存在则直接返回
        if (!shopList.isEmpty()) {
            for (String item : shopList) {
                ShopType shopType = JSONUtil.toBean(item, ShopType.class);
                res.add(shopType);
            }
            return Result.ok(res);
        }
        // 2. 缓存不存在，查询数据库
        res = typeService
                .query()
                .orderByAsc("sort")
                .list();
        // 2.1 数据库不存在，返回默认结果
        if (res.isEmpty()) {
            return Result.fail("商品类型不存在！");
        }
        // 2.2 数据库存在，将数据存入缓存，再返回
        for (ShopType shopType : res) {
            String item = JSONUtil.toJsonStr(shopType);
            redisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, item);
        }
        return Result.ok(res);
    }
}
