package com.shop.youssef.shop_service.web.dto;

import java.math.BigDecimal;
import java.util.List;


public class OrderDtos {

    public record CreateOrderRequest(
            String customerEmail,
            Long walletId,
            List<OrderItemRequest> items
    ) {}

    public record OrderItemRequest(
            Long productId,
            Integer quantity
    ) {}

    public record OrderItemResponse(
            Long id,
            String sku,
            String name,
            BigDecimal price,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            Long id,
            String customerEmail,
            BigDecimal total,
            String status,
            List<OrderItemResponse> items
    ) {}
}
