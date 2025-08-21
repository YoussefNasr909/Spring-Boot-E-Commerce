package com.inventory.youssef.inventory_service.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @GetMapping("/inventory/ping")
    public String ping() {
         return "inventory service is ok";
     }

}
