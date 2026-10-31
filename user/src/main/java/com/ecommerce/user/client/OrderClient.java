package com.ecommerce.user.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ORDER")
public interface OrderClient {

    @PostMapping("/orders")
    OrderResponse createOrder(
        @RequestHeader("Authorization") String authorization,
        @RequestBody PlaceOrderRequest request
    );

    @GetMapping("/orders")
    List<OrderResponse> getOrders(
        @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/orders/{orderId}")
    OrderResponse getOrder(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("orderId") Long orderId
    );

    record PlaceOrderRequest(List<OrderItemRequest> items) {}

    record OrderItemRequest(Long productId, int quantity, Double price) {}

    record OrderResponse(
        Long id,
        Double total,
        Long user_id,
        String status,
        List<OrderLineResponse> order_item
    ) {}

    record OrderLineResponse(
        Long id,
        int quantity,
        Long product_id,
        Double price
    ) {}
}