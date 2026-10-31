package com.ecommerce.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.client.OrderClient;
import com.ecommerce.user.client.OrderClient.OrderResponse;
import com.ecommerce.user.client.OrderClient.PlaceOrderRequest;
import com.ecommerce.user.model.User;
import com.ecommerce.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class userController {

    private final UserService userservice;
    private final OrderClient orderClient;

    public userController(UserService userservice, OrderClient orderClient) {
        this.userservice = userservice;
        this.orderClient = orderClient;
    }

    @GetMapping("{Id}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long Id,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnUser(Id, jwt);
        return ResponseEntity.ok(userservice.getUserById(Id));
    }

    @DeleteMapping ("{Id}")
    public ResponseEntity<User> deleteUserById(
            @PathVariable Long Id,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnUser(Id, jwt);
        userservice.deleteUserById(Id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<User>> getUserAll(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
            List.of(userservice.getUserById(userId(jwt))));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userservice.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody PlaceOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnUser(userId, jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderClient.createOrder(authorization, request));
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnUser(userId, jwt);
        return ResponseEntity.ok(orderClient.getOrders(authorization));
    }

    @GetMapping("/{userId}/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnUser(userId, jwt);
        return ResponseEntity.ok(orderClient.getOrder(authorization, orderId));
    }

    @PutMapping ("{Id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long Id,
            @RequestBody User user,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnUser(Id, jwt);
        return ResponseEntity.ok(userservice.updateUser(Id, user));
    }

    private void requireOwnUser(Long requestedId, Jwt jwt) {
        if (!requestedId.equals(userId(jwt))) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString("userId"));
    }
}
