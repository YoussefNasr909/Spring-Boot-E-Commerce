package com.wallet.youssef.wallet_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
    @GetMapping("/wallet/ping")
    public String ping() {
        return "wallet service is ok";
    }

}
