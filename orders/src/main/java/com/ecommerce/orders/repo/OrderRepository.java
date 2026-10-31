package com.ecommerce.orders.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

import com.ecommerce.orders.model.Order;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user_id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user_id = :userId")
    Optional<Order> findByIdAndUserId(
        @Param("id") Long id,
        @Param("userId") Long userId
    );
}