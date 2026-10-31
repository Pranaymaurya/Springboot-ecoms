
package com.ecommerce.orders.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ecommerce.orders.client.InventoryClient;
import com.ecommerce.orders.dto.CreateOrderRequest;
import com.ecommerce.orders.dto.OrderItemRequest;
import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.model.Order_item;
import com.ecommerce.orders.repo.OrderRepository;

import feign.FeignException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final TransactionTemplate transactionTemplate;

    public OrderService(
            OrderRepository orderRepository,
            InventoryClient inventoryClient,
            TransactionTemplate transactionTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.transactionTemplate = transactionTemplate;
    }

    public Order createOrder(
            Long userId,
            String authorization,
            CreateOrderRequest request) {

        if (userId == null ||
            authorization == null ||
            authorization.isBlank() ||
            request == null ||
            request.items() == null ||
            request.items().isEmpty()) {
            throw new IllegalArgumentException(
                "Order items are required");
        }

        double total = 0;

        for (OrderItemRequest item : request.items()) {
            if (item.productId() == null ||
                item.quantity() <= 0 ||
                item.price() == null ||
                !Double.isFinite(item.price()) ||
                item.price() < 0) {
                throw new IllegalArgumentException(
                    "Invalid order item");
            }

            total += item.price() * item.quantity();
        }

        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException(
                "Invalid order total");
        }

        final double orderTotal = total;

        // Save the order and all its items in one local transaction.
        Order savedOrder = transactionTemplate.execute(status -> {
            Order order = new Order();
            order.setUser_id(userId);
            order.setTotal(orderTotal);
            order.setStatus("PENDING");

            List<Order_item> items = new ArrayList<>();

            for (OrderItemRequest itemRequest : request.items()) {
                Order_item item = new Order_item();
                item.setOrder(order);
                item.setProduct_id(itemRequest.productId());
                item.setQuantity(itemRequest.quantity());
                item.setPrice(itemRequest.price());
                items.add(item);
            }

            order.setOrder_item(items);
            return orderRepository.save(order);
        });

        try {
            InventoryClient.ReservationRequest reservationRequest =
                new InventoryClient.ReservationRequest(
                    savedOrder.getId(),
                    request.items().stream()
                        .map(item ->
                            new InventoryClient.ReservationItem(
                                item.productId(),
                                item.quantity()
                            )
                        )
                        .toList()
                );

            inventoryClient.reserve(authorization, reservationRequest);

            transactionTemplate.executeWithoutResult(status -> {
                Order order = orderRepository.findById(
                    savedOrder.getId()
                ).orElseThrow();

                order.setStatus("RESERVED");
                orderRepository.save(order);
            });
            savedOrder.setStatus("RESERVED");

            return savedOrder;

        } catch (FeignException ex) {

            // The inventory request may have failed before or after
            // Inventory Service committed its reservation. In a
            // production workflow, reconcile this outcome before
            // assuming that no reservation exists.
            transactionTemplate.executeWithoutResult(status -> {
                Order order = orderRepository.findById(
                    savedOrder.getId()
                ).orElseThrow();

                order.setStatus("CANCELLATION_PENDING");
                orderRepository.save(order);
            });

            throw new RuntimeException(
                "Order created but inventory reservation outcome " +
                "needs reconciliation. Order ID: " + savedOrder.getId(),
                ex
            );
        }
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id, Long userId) {
        return orderRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
