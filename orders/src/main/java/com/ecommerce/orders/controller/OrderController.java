package com.ecommerce.orders.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.orders.dto.CreateOrderRequest;
import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {
        return orderService.createOrder(userId(jwt), authorization, request);
    }

    @GetMapping
    public List<Order> getOrders(
            @AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrders(userId(jwt));
    }

    @GetMapping("/{id}")
    public Order getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrder(id, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString("userId"));
    }
}