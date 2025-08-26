package com.wallet.youssef.wallet_service.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class PingController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        // response بسيط ومقروء عشان نعرف أي instance ردّت
        return Map.of(
                "message", "Wallet service is ok",
                "port", port,
                "service", "wallet-service"
        );
    }
}
