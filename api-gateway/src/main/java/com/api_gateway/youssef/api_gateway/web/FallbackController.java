package com.api_gateway.youssef.api_gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gateway-fallback")
public class FallbackController {

    @RequestMapping("/shop")
    public ResponseEntity<Map<String, String>> shopFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Shop upstream unavailable (propagated by API Gateway)"));
    }

    @RequestMapping("/wallet")
    public ResponseEntity<Map<String, String>> walletFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Wallet service unavailable (via API Gateway)"));
    }

    @RequestMapping("/inventory")
    public ResponseEntity<Map<String, String>> inventoryFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Inventory service unavailable (via API Gateway)"));
    }
}
