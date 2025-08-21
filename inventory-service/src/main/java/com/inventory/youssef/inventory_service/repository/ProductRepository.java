package com.inventory.youssef.inventory_service.repository;

import com.inventory.youssef.inventory_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku); // مفيد لاحقًا
    boolean existsBySku(String sku);
}
