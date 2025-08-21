package com.shop.youssef.shop_service.repository;

import com.shop.youssef.shop_service.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // هتجيب كل الأوردرز ومعاها الـ items في نفس الكويري
    @EntityGraph(attributePaths = "items")
    @Query("select distinct o from Order o")
    List<Order> findAllWithItems();

    // هتجيب أوردر واحد ومعاه الـ items
    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
