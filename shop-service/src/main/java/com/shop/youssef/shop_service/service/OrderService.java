package com.shop.youssef.shop_service.service;

import com.shop.youssef.shop_service.model.Order;
import com.shop.youssef.shop_service.model.OrderItem;
import com.shop.youssef.shop_service.repository.OrderRepository;
import com.shop.youssef.shop_service.web.dto.OrderDtos.CreateOrderRequest;
import com.shop.youssef.shop_service.web.dto.OrderDtos.OrderItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public Order create(CreateOrderRequest req) {
        // تحقق بدائي
        if (req == null || req.customerEmail == null || req.customerEmail.isBlank()
                || req.items == null || req.items.isEmpty()) {
            throw new IllegalArgumentException("customerEmail and items are required");
        }

        Order o = new Order();
        o.setCustomerEmail(req.customerEmail);
        o.setStatus("PENDING"); // لسه مفيش دفع
        o.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest it : req.items) {
            if (it.sku == null || it.sku.isBlank()
                    || it.name == null || it.name.isBlank()
                    || it.price == null || it.quantity == null) {
                throw new IllegalArgumentException("item fields are required");
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(o); // اربطها بالأوردر
            oi.setSku(it.sku);
            oi.setName(it.name);
            oi.setPrice(it.price);
            oi.setQuantity(it.quantity);
            oi.setLineTotal(it.price.multiply(BigDecimal.valueOf(it.quantity)));
            o.getItems().add(oi);

            total = total.add(oi.getLineTotal());
        }

        o.setTotal(total);
        return orders.save(o);
    }

    @Transactional(readOnly = true)
    public Order getByIdWithItems(Long id) {
        return orders.findByIdWithItems(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    @Transactional
    public Order update(Long id, CreateOrderRequest req) {
        if (req == null || req.customerEmail == null || req.customerEmail.isBlank()
                || req.items == null || req.items.isEmpty()) {
            throw new IllegalArgumentException("customerEmail and items are required");
        }

        Order o = getByIdWithItems(id);
        o.setCustomerEmail(req.customerEmail);

        // امسح القديم وبنّي الجديد
        o.getItems().clear();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest it : req.items) {
            if (it.sku == null || it.sku.isBlank()
                    || it.name == null || it.name.isBlank()
                    || it.price == null || it.quantity == null) {
                throw new IllegalArgumentException("item fields are required");
            }

            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setSku(it.sku);
            oi.setName(it.name);
            oi.setPrice(it.price);
            oi.setQuantity(it.quantity);
            oi.setLineTotal(it.price.multiply(BigDecimal.valueOf(it.quantity)));
            o.getItems().add(oi);

            total = total.add(oi.getLineTotal());
        }

        o.setTotal(total);
        return orders.save(o);
    }

    @Transactional
    public void delete(Long id) {
        if (!orders.existsById(id)) {
            throw new NoSuchElementException("Order not found");
        }
        orders.deleteById(id);
    }
}
