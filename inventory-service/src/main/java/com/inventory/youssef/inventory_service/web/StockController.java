package com.inventory.youssef.inventory_service.web;

import com.inventory.youssef.inventory_service.model.Product;
import com.inventory.youssef.inventory_service.repository.ProductRepository;
import com.inventory.youssef.inventory_service.web.dto.StockDtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory") // مهم عشان البوابة واقفة على /inventory/**
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final ProductRepository products;

    public StockController(ProductRepository products) {
        this.products = products;
    }

    // 1) فحص المخزون + إرجاع بيانات المنتجات
    @PostMapping("/stock/check")
    @Transactional(readOnly = true)
    public List<StockCheckResponse> check(@RequestBody List<StockCheckRequest> reqs) {
        // Logging تشخيصي واضح
        log.info("CHECK request size={}, body={}", (reqs == null ? 0 : reqs.size()), reqs);

        if (reqs == null || reqs.isEmpty()) {
            log.warn("CHECK: empty request -> return empty list");
            return List.of();
        }

        // هات كل IDs المطلوبة
        List<Long> ids = reqs.stream()
                .map(StockCheckRequest::productId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            log.warn("CHECK: all productId were null -> return empty list");
            return List.of();
        }

        // هات المنتجات الموجودة فعلاً
        List<Product> found = products.findAllById(ids);
        Map<Long, Product> byId = found.stream().collect(Collectors.toMap(Product::getId, p -> p));

        // بنبني الرد على الموجود فقط
        List<StockCheckResponse> out = new ArrayList<>();
        for (StockCheckRequest r : reqs) {
            Product p = byId.get(r.productId());
            if (p != null) {
                out.add(new StockCheckResponse(
                        p.getId(), p.getSku(), p.getName(), p.getPrice(), p.getQuantity()
                ));
            }
        }

        log.info("CHECK: requestedIds={}, foundCount={}, responseCount={}",
                ids, found.size(), out.size());

        return out;
    }

    // 2) خصم المخزون
    @PostMapping("/stock/consume")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void consume(@RequestBody List<ConsumeRequest> reqs) {
        log.info("CONSUME request size={}, body={}", (reqs == null ? 0 : reqs.size()), reqs);

        if (reqs == null || reqs.isEmpty()) return;

        Map<Long, Integer> toConsume = new HashMap<>();
        for (ConsumeRequest r : reqs) {
            if (r.productId() != null && r.quantity() != null && r.quantity() > 0) {
                toConsume.merge(r.productId(), r.quantity(), Integer::sum);
            }
        }

        if (toConsume.isEmpty()) return;

        List<Product> found = products.findAllById(toConsume.keySet());

        // تحقق من الكميات
        for (Product p : found) {
            int q = toConsume.getOrDefault(p.getId(), 0);
            if (p.getQuantity() < q) {
                throw new IllegalArgumentException("Not enough stock for product " + p.getId());
            }
        }

        // خصم فعلي وحفظ
        for (Product p : found) {
            int q = toConsume.get(p.getId());
            p.setQuantity(p.getQuantity() - q);
        }
        products.saveAll(found);

        log.info("CONSUME: consumed for ids={}", toConsume.keySet());
    }
}
