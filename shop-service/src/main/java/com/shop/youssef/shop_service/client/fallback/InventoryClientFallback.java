package com.shop.youssef.shop_service.client.fallback;

import com.shop.youssef.shop_service.client.InventoryClient;
import com.shop.youssef.shop_service.client.dto.ConsumeRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public List<StockCheckResponse> check(List<StockCheckRequest> reqs) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory service unavailable. Please try again."
        );
    }

    @Override
    public void consume(List<ConsumeRequest> reqs) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory service unavailable. Please try again."
        );
    }
}
