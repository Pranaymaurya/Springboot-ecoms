package com.ecommerce.orders.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.orders.model.Order_item;

public interface OrderItemRepository
        extends JpaRepository<Order_item, Long> {
}