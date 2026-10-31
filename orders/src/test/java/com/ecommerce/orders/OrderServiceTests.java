package com.ecommerce.orders;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import com.ecommerce.orders.client.InventoryClient;
import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.repo.OrderRepository;
import com.ecommerce.orders.service.OrderService;

class OrderServiceTests {

    @Test
    void orderLookupIsScopedToAuthenticatedCustomer() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        Order order = new Order();
        order.setId(3L);
        order.setUser_id(7L);
        when(orderRepository.findByIdAndUserId(3L, 7L))
            .thenReturn(Optional.of(order));

        OrderService orderService = new OrderService(
            orderRepository,
            mock(InventoryClient.class),
            mock(TransactionTemplate.class)
        );

        assertSame(order, orderService.getOrder(3L, 7L));
        verify(orderRepository).findByIdAndUserId(3L, 7L);
    }
}
