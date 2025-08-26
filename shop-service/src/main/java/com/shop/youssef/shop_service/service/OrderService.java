package com.shop.youssef.shop_service.service;

import com.shop.youssef.shop_service.client.InventoryClient;
import com.shop.youssef.shop_service.client.WalletClient;
import com.shop.youssef.shop_service.client.dto.ConsumeRequest;
import com.shop.youssef.shop_service.client.dto.MoneyRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckResponse;
import com.shop.youssef.shop_service.model.Order;
import com.shop.youssef.shop_service.model.OrderItem;
import com.shop.youssef.shop_service.repository.OrderRepository;
import com.shop.youssef.shop_service.web.dto.OrderDtos.CreateOrderRequest;
import com.shop.youssef.shop_service.web.dto.OrderDtos.OrderItemRequest;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final InventoryClient inventory;
    private final WalletClient wallet;

    public OrderService(OrderRepository orders, InventoryClient inventory, WalletClient wallet) {
        this.orders = orders;
        this.inventory = inventory;
        this.wallet = wallet;
    }

    @Transactional
    public Order create(CreateOrderRequest req) {
        // 0) تحقق بسيط من المدخلات
        if (req == null || req.customerEmail() == null || req.customerEmail().isBlank()
                || req.items() == null || req.items().isEmpty() || req.walletId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "customerEmail, walletId and items are required");
        }

        // 1) استعلم عن المنتجات من inventory
        var stockReqs = req.items().stream()
                .map(i -> new StockCheckRequest(i.productId(), i.quantity()))
                .toList();

        final List<StockCheckResponse> stock;
        try {
            stock = inventory.check(stockReqs);
        } catch (FeignException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Inventory service unavailable. Please try again.", ex);
        }

        System.out.println("[SHOP] requested IDs = " +
                stockReqs.stream().map(StockCheckRequest::productId).toList());
        System.out.println("[SHOP] inventory IDs = " +
                (stock == null ? "null" : stock.stream().map(StockCheckResponse::productId).toList()));

        if (stock == null || stock.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Products not found in inventory");
        }

        Map<Long, StockCheckResponse> byProductId = new HashMap<>();
        for (StockCheckResponse s : stock) byProductId.put(s.productId(), s);

        // 2) ابنِ الـOrder واحسب الإجمالي من أسعار الـinventory
        Order o = new Order();
        o.setCustomerEmail(req.customerEmail());
        o.setStatus("PENDING");
        o.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest it : req.items()) {
            StockCheckResponse info = byProductId.get(it.productId());
            if (info == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + it.productId() + " not found");
            }
            if (info.available() < it.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not enough stock for product " + it.productId());
            }

            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setSku(info.sku());
            oi.setName(info.name());
            oi.setPrice(info.price());
            oi.setQuantity(it.quantity());
            oi.setLineTotal(info.price().multiply(BigDecimal.valueOf(it.quantity())));
            o.getItems().add(oi);

            total = total.add(oi.getLineTotal());
        }

        // 3) اسحب الفلوس من المحفظة
        try {
            wallet.withdraw(req.walletId(), new MoneyRequest(total));
        } catch (FeignException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Wallet service unavailable. Please try again.", ex);
        }

        // 4) خصم المخزون؛ لو فشل → رجّع الفلوس وارمي 503
        var consumeReqs = req.items().stream()
                .map(i -> new ConsumeRequest(i.productId(), i.quantity()))
                .toList();

        try {
            inventory.consume(consumeReqs);
            o.setStatus("CONFIRMED");
        } catch (FeignException | IllegalStateException ex) {
            try { wallet.deposit(req.walletId(), new MoneyRequest(total)); } catch (Exception ignore) {}
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Inventory consume failed, wallet refunded", ex);
        }

        o.setTotal(total);
        return orders.save(o);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orders.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional
    public Order update(Long id, CreateOrderRequest req) {
        if (req == null || req.customerEmail() == null || req.customerEmail().isBlank()
                || req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "customerEmail and items are required");
        }

        Order o = orders.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        o.setCustomerEmail(req.customerEmail());
        o.getItems().clear();

        var stockReqs = req.items().stream()
                .map(i -> new StockCheckRequest(i.productId(), i.quantity()))
                .toList();

        final List<StockCheckResponse> stock;
        try {
            stock = inventory.check(stockReqs);
        } catch (FeignException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Inventory service unavailable. Please try again.", ex);
        }

        Map<Long, StockCheckResponse> byId = new HashMap<>();
        for (StockCheckResponse s : stock) byId.put(s.productId(), s);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest it : req.items()) {
            var info = byId.get(it.productId());
            if (info == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + it.productId() + " not found");
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setSku(info.sku());
            oi.setName(info.name());
            oi.setPrice(info.price());
            oi.setQuantity(it.quantity());
            oi.setLineTotal(info.price().multiply(BigDecimal.valueOf(it.quantity())));
            o.getItems().add(oi);

            total = total.add(oi.getLineTotal());
        }

        o.setTotal(total);
        return orders.save(o);
    }

    @Transactional
    public void delete(Long id) {
        if (!orders.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        orders.deleteById(id);
    }
}
