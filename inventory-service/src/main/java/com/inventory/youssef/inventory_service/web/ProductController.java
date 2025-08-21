package com.inventory.youssef.inventory_service.web;

import com.inventory.youssef.inventory_service.model.Product;
import com.inventory.youssef.inventory_service.repository.ProductRepository;
import com.inventory.youssef.inventory_service.web.dto.ProductDtos.ProductRequest;
import com.inventory.youssef.inventory_service.web.dto.ProductDtos.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/inventory/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest req) {
        if (req.sku == null || req.sku.isBlank() || req.name == null || req.name.isBlank()
                || req.price == null || req.quantity == null) {
            return ResponseEntity.badRequest().body("sku, name, price, quantity are required");
        }
        if (repo.existsBySku(req.sku)) {
            return ResponseEntity.badRequest().body("sku already exists");
        }
        Product p = new Product();
        p.setSku(req.sku);
        p.setName(req.name);
        p.setPrice(req.price);
        p.setQuantity(req.quantity);
        Product saved = repo.save(p);

        URI location = URI.create("/inventory/products/" + saved.getId());
        return ResponseEntity.created(location)
                .body(toResponse(saved));
    }

    // READ ALL
    @GetMapping
    public List<ProductResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return repo.findById(id)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(toResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductRequest req) {
        return repo.findById(id).map(p -> {
            if (req.name != null) p.setName(req.name);
            if (req.price != null) p.setPrice(req.price);
            if (req.quantity != null) p.setQuantity(req.quantity);
            if (req.sku != null && !req.sku.equals(p.getSku())) {
                // لو غيّرنا الـ sku نتأكد إنه مش مستخدم
                if (repo.existsBySku(req.sku)) {
                    return ResponseEntity.badRequest().body("sku already exists");
                }
                p.setSku(req.sku);
            }
            Product saved = repo.save(p);
            return ResponseEntity.ok(toResponse(saved));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getPrice(), p.getQuantity());
    }
}
