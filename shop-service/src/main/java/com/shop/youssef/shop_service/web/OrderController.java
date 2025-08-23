package com.shop.youssef.shop_service.web;

import com.shop.youssef.shop_service.model.Order;
import com.shop.youssef.shop_service.model.OrderItem;
import com.shop.youssef.shop_service.repository.OrderRepository;
import com.shop.youssef.shop_service.service.OrderService;
import com.shop.youssef.shop_service.web.dto.OrderDtos.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/shop/orders")
public class OrderController {

    private final OrderService service;
    private final OrderRepository orders;

    public OrderController(OrderService service, OrderRepository orders) {
        this.service = service;
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {
        try {
            Order saved = service.create(req);
            return ResponseEntity.created(URI.create("/shop/orders/" + saved.getId()))
                    .body(toResponse(saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @GetMapping
    public List<OrderResponse> list() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return orders.findById(id)
                .<ResponseEntity<?>>map(o -> ResponseEntity.ok(toResponse(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CreateOrderRequest req) {
        try {
            Order updated = service.update(id, req);
            return ResponseEntity.ok(toResponse(updated));
        } catch (NoSuchElementException notFound) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException bad) {
            return ResponseEntity.badRequest().body(bad.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException notFound) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== تحويل الكيانات إلى DTOs للرد =====
    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(this::toItemResponse).toList();
        return new OrderResponse(o.getId(), o.getCustomerEmail(), o.getTotal(),
                o.getStatus(), items);
    }

    private OrderItemResponse toItemResponse(OrderItem it) {
        return new OrderItemResponse(it.getId(), it.getSku(), it.getName(),
                it.getPrice(), it.getQuantity(), it.getLineTotal());
    }
}
