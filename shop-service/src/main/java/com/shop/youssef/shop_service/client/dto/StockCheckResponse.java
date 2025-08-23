package com.shop.youssef.shop_service.client.dto;

import java.math.BigDecimal;

public record StockCheckResponse(Long productId, String sku, String name,
                                 Integer available, BigDecimal price) {}
