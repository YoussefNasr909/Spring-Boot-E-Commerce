package com.inventory.youssef.inventory_service.web;

import com.inventory.youssef.inventory_service.model.Product;
import com.inventory.youssef.inventory_service.repository.ProductRepository;
import com.inventory.youssef.inventory_service.web.dto.StockDtos.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory/stock")
public class StockController {

    private final ProductRepository products;

    public StockController(ProductRepository products) {
        this.products = products;
    }

    @PostMapping("/check")
    public List<StockCheckResponse> check(@RequestBody List<StockCheckRequest> req) {
        if (req == null || req.isEmpty()) return List.of();

        List<Long> ids = req.stream().map(StockCheckRequest::productId).toList();

        List<Product> found = products.findAllById(ids);

        return found.stream()
                .map(p -> new StockCheckResponse(
                        p.getId(), p.getSku(), p.getName(), p.getQuantity(), p.getPrice()
                ))
                .toList();
    }

    @PostMapping("/consume")
    @Transactional
    public void consume(@RequestBody List<ConsumeRequest> req) {
        if (req == null || req.isEmpty()) return;

        Map<Long, Integer> qtyById = req.stream()
                .collect(Collectors.toMap(ConsumeRequest::productId, ConsumeRequest::quantity, Integer::sum));

        for (Map.Entry<Long, Integer> e : qtyById.entrySet()) {
            Product p = products.findById(e.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Product " + e.getKey() + " not found"));

            int need = e.getValue();
            if (p.getQuantity() < need) {
                throw new IllegalStateException("Not enough stock for product " + p.getId());
            }

            p.setQuantity(p.getQuantity() - need);
            products.save(p);
        }
    }
}
