package com.shop.youssef.shop_service.client.fallback;

import com.shop.youssef.shop_service.client.InventoryClient;
import com.shop.youssef.shop_service.client.dto.ConsumeRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public List<StockCheckResponse> check(List<StockCheckRequest> reqs) {
        return Collections.emptyList();
    }

    @Override
    public void consume(List<ConsumeRequest> reqs) {
        // no-op
    }
}
