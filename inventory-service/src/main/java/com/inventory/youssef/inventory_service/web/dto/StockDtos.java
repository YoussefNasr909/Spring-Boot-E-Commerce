package com.inventory.youssef.inventory_service.web.dto;

import java.math.BigDecimal;

public class StockDtos {

    public record StockCheckRequest(Long productId, Integer quantity) {}

    public record StockCheckResponse(
            Long productId,
            String sku,
            String name,
            Integer available,
            BigDecimal price
    ) {}

    public record ConsumeRequest(Long productId, Integer quantity) {}
}
