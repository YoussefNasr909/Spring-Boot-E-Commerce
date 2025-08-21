package com.shop.youssef.shop_service.web.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderDtos {

    // JSON اللي هييجي في POST
    public static class OrderItemRequest {
        public String sku;
        public String name;
        public BigDecimal price;
        public Integer quantity;
    }

    public static class CreateOrderRequest {
        public String customerEmail;
        public List<OrderItemRequest> items;
    }

    // شكل الرد
    public static class OrderItemResponse {
        public Long id;
        public String sku;
        public String name;
        public BigDecimal price;
        public Integer quantity;
        public BigDecimal lineTotal;

        public OrderItemResponse(Long id, String sku, String name,
                                 BigDecimal price, Integer quantity, BigDecimal lineTotal) {
            this.id = id; this.sku = sku; this.name = name;
            this.price = price; this.quantity = quantity; this.lineTotal = lineTotal;
        }
    }

    public static class OrderResponse {
        public Long id;
        public String customerEmail;
        public BigDecimal total;
        public String status;
        public List<OrderItemResponse> items;

        public OrderResponse(Long id, String customerEmail, BigDecimal total,
                             String status, List<OrderItemResponse> items) {
            this.id = id; this.customerEmail = customerEmail; this.total = total;
            this.status = status; this.items = items;
        }
    }
}
