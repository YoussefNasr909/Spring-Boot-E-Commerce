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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // تحقق بسيط على المدخلات
        if (req == null || req.customerEmail() == null || req.customerEmail().isBlank()
                || req.items() == null || req.items().isEmpty() || req.walletId() == null) {
            throw new IllegalArgumentException("customerEmail, walletId and items are required");
        }

        // 1) استعلم عن كل المنتجات المطلوبة من inventory (الكمية/السعر/sku/name)
        var stockReqs = req.items().stream()
                .map(i -> new StockCheckRequest(i.productId(), i.quantity()))
                .toList();

        List<StockCheckResponse> stock = inventory.check(stockReqs);
        if (stock == null || stock.isEmpty()) {
            throw new IllegalArgumentException("Products not found in inventory");
        }

        Map<Long, StockCheckResponse> byProductId = new HashMap<>();
        for (StockCheckResponse s : stock) {
            byProductId.put(s.productId(), s);
        }

        // 2) ابني الـ Order واحسب الإجمالي من الأسعار القادمة من الـ inventory
        Order o = new Order();
        o.setCustomerEmail(req.customerEmail());
        o.setStatus("PENDING");
        o.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest it : req.items()) {
            StockCheckResponse info = byProductId.get(it.productId());
            if (info == null) {
                throw new IllegalArgumentException("Product " + it.productId() + " not found");
            }
            if (info.available() < it.quantity()) {
                throw new IllegalArgumentException("Not enough stock for product " + it.productId());
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

        // 3) اسحب المبلغ من المحفظة
        wallet.withdraw(req.walletId(), new MoneyRequest(total));

        // 4) خصم المخزون؛ لو فشل، رجّع فلوس العميل وارمي خطأ
        var consumeReqs = req.items().stream()
                .map(i -> new ConsumeRequest(i.productId(), i.quantity()))
                .toList();

        try {
            inventory.consume(consumeReqs);
            o.setStatus("CONFIRMED");
        } catch (Exception ex) {
            wallet.deposit(req.walletId(), new MoneyRequest(total));
            throw new IllegalStateException("Inventory consume failed, wallet refunded");
        }

        o.setTotal(total);
        return orders.save(o);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        // ملاحظة: القراءة هتتم تحت @Transactional من الكنترولر لتفادي Lazy
    }

    @Transactional
    public Order update(Long id, CreateOrderRequest req) {
        if (req == null || req.customerEmail() == null || req.customerEmail().isBlank()
                || req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("customerEmail and items are required");
        }

        // تبسيط: نعدّل البنود والإجمالي فقط (بدون تعامل مالي/مخزون في التعديل)
        Order o = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        o.setCustomerEmail(req.customerEmail());
        o.getItems().clear();

        // إعادة حساب الإجمالي بالأسعار الحالية من الـ inventory
        var stockReqs = req.items().stream()
                .map(i -> new StockCheckRequest(i.productId(), i.quantity()))
                .toList();
        var stock = inventory.check(stockReqs);
        Map<Long, StockCheckResponse> byId = new HashMap<>();
        for (StockCheckResponse s : stock) byId.put(s.productId(), s);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest it : req.items()) {
            var info = Optional.ofNullable(byId.get(it.productId()))
                    .orElseThrow(() -> new IllegalArgumentException("Product " + it.productId() + " not found"));

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
        // تبسيط: حذف بدون رد مخزون/أموال (هييجي لاحقًا لو عايزين نكمّل سيناريو كامل)
        orders.deleteById(id);
    }
}
