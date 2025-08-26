package com.shop.youssef.shop_service.client;

import com.shop.youssef.shop_service.client.dto.*;
import com.shop.youssef.shop_service.client.fallback.InventoryClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @PostMapping("/inventory/stock/check")
    List<StockCheckResponse> check(@RequestBody List<StockCheckRequest> reqs);

    @PostMapping("/inventory/stock/consume")
    void consume(@RequestBody List<ConsumeRequest> reqs);
}
