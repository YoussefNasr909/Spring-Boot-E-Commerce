package com.inventory.youssef.inventory_service.web.dto;

import java.math.BigDecimal;

public class ProductDtos {

    // بيانات داخل الطلب (Create/Update)
    public static class ProductRequest {
        public String sku;
        public String name;
        public BigDecimal price;
        public Integer quantity;
    }

    // شكل الرد
    public static class ProductResponse {
        public Long id;
        public String sku;
        public String name;
        public BigDecimal price;
        public Integer quantity;

        public ProductResponse(Long id, String sku, String name, BigDecimal price, Integer quantity) {
            this.id = id; this.sku = sku; this.name = name; this.price = price; this.quantity = quantity;
        }
    }
}
