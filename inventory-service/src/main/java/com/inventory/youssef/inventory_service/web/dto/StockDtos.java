package com.inventory.youssef.inventory_service.web.dto;

import java.math.BigDecimal;

public class StockDtos {

    // الشوب بيبعت productId + quantity المطلوب حجزها/التأكد منها
    public record StockCheckRequest(Long productId, Integer quantity) {}

    // ردّ check: بنرجّع الداتا اللي الشوب محتاجها يبني منها OrderItem
    public record StockCheckResponse(
            Long productId,
            String sku,
            String name,
            BigDecimal price,
            Integer available
    ) {}

    // طلب الخصم الفعلي من المخزون
    public record ConsumeRequest(Long productId, Integer quantity) {}
}
