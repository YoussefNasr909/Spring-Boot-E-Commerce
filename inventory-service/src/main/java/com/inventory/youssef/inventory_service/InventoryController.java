package com.inventory.youssef.inventory_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @GetMapping("/inventory/ping")
    public String ping() {
         return "inventory service is ok";
     }

}
