package com.inventory.youssef.inventory_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // يخلي MySQL يزود الـ id تلقائي
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku; // كود المنتج

    @Column(nullable = false, length = 200)
    private String name; // اسم المنتج

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // السعر

    @Column(nullable = false)
    private Integer quantity; // الكمية بالمخزون

    // Getters/Setters — بسيطين عشان متحتّجش Lombok دلوقتي
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
