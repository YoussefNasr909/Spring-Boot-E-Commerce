package com.shop.youssef.shop_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopController {
    @GetMapping("/shop/ping")
    public String ping() {
        return "shop service is ok";
    }
}