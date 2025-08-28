package com.shop.youssef.shop_service.service;

import com.shop.youssef.shop_service.client.dto.ConsumeRequest;
import com.shop.youssef.shop_service.client.dto.MoneyRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckResponse;
import com.shop.youssef.shop_service.model.Order;
import com.shop.youssef.shop_service.model.OrderItem;
import com.shop.youssef.shop_service.repository.OrderRepository;
import com.shop.youssef.shop_service.web.dto.OrderDtos.CreateOrderRequest;
import com.shop.youssef.shop_service.web.dto.OrderDtos.OrderItemRequest;
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
    private final RemoteClients remote; // بديل عن حقن Feign مباشرة هنا

    public OrderService(OrderRepository orders, RemoteClients remote) {
        this.orders = orders;
        this.remote = remote;
    }

    @Transactional
    public Order create(CreateOrderRequest req) {
        // تحقق أساسي
        if (req == null || req.customerEmail() == null || req.customerEmail().isBlank()
                || req.items() == null || req.items().isEmpty() || req.walletId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "customerEmail, walletId and items are required");
        }

        // 1) جلب بيانات المخزون (لو الخدمة واقعة → 503 من fallback)
        var stockReqs = req.items().stream()
                .map(i -> {
                    if (i.quantity() == null || i.quantity() <= 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
                    }
                    return new StockCheckRequest(i.productId(), i.quantity());
                })
                .toList();

        List<StockCheckResponse> stock = remote.inventoryCheck(stockReqs);

        // لو الخدمة شغالة لكن رجّعت لا شيء → 400 منطقي
        if (stock == null || stock.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Products not found in inventory");
        }

        Map<Long, StockCheckResponse> byProductId = new HashMap<>();
        for (StockCheckResponse s : stock) byProductId.put(s.productId(), s);

        // 2) بناء الأوردر + حساب الإجمالي من أسعار الـInventory
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

        // 3) سحب من المحفظة (لو واقعة → 503 من fallback)
        remote.walletWithdraw(req.walletId(), new MoneyRequest(total));

        // 4) خصم المخزون؛ لو فشل (503) نرجّع فلوس العميل ثم نرمي 503
        var consumeReqs = req.items().stream()
                .map(i -> new ConsumeRequest(i.productId(), i.quantity()))
                .toList();

        try {
            remote.inventoryConsume(consumeReqs);
            o.setStatus("CONFIRMED");
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                // Refund فقط لو مشكلة الـInventory
                remote.walletDeposit(req.walletId(), new MoneyRequest(total));
            }
            throw ex;
        }

        o.setTotal(total);
        return orders.save(o);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    @Transactional
    public Order update(Long id, CreateOrderRequest req) {
        if (req == null || req.customerEmail() == null || req.customerEmail().isBlank()
                || req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerEmail and items are required");
        }

        Order o = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        o.setCustomerEmail(req.customerEmail());
        o.getItems().clear();

        var stockReqs = req.items().stream()
                .map(i -> new StockCheckRequest(i.productId(), i.quantity()))
                .toList();

        var stock = remote.inventoryCheck(stockReqs);
        Map<Long, StockCheckResponse> byId = new HashMap<>();
        for (StockCheckResponse s : stock) byId.put(s.productId(), s);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest it : req.items()) {
            var info = Optional.ofNullable(byId.get(it.productId()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Product " + it.productId() + " not found"));

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
        if (!orders.existsById(id)) throw new NoSuchElementException("Order not found");
        orders.deleteById(id);
    }
}
