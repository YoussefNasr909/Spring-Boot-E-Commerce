package com.shop.youssef.shop_service.repository;

import com.shop.youssef.shop_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> { }
