package com.ecommerce.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.user.client.OrderClient;
import com.ecommerce.user.client.OrderClient.OrderResponse;
import com.ecommerce.user.client.OrderClient.PlaceOrderRequest;
import com.ecommerce.user.controller.userController;
import com.ecommerce.user.service.UserService;

import org.springframework.security.oauth2.jwt.Jwt;

class UserOrderControllerTests {

    @Test
    void createsOrderThroughOrdersServiceUsingAuthenticatedToken() {
        OrderClient orderClient = mock(OrderClient.class);
        userController controller =
            new userController(mock(UserService.class), orderClient);
        String authorization = "Bearer customer-token";
        PlaceOrderRequest request = new PlaceOrderRequest(List.of());
        OrderResponse order = new OrderResponse(
            10L, 25.0, 7L, "RESERVED", List.of());
        when(orderClient.createOrder(authorization, request))
            .thenReturn(order);

        var response = controller.createOrder(
            7L, authorization, request, customerJwt(7L));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(order, response.getBody());
        verify(orderClient).createOrder(authorization, request);
    }

    @Test
    void doesNotForwardOrderRequestForAnotherCustomer() {
        OrderClient orderClient = mock(OrderClient.class);
        userController controller =
            new userController(mock(UserService.class), orderClient);

        assertThrows(ResponseStatusException.class, () ->
            controller.createOrder(
                8L,
                "Bearer customer-token",
                new PlaceOrderRequest(List.of()),
                customerJwt(7L)
            )
        );

        verify(orderClient, never()).createOrder(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    private Jwt customerJwt(Long userId) {
        return Jwt.withTokenValue("customer-token")
            .header("alg", "HS256")
            .claim("userId", userId.toString())
            .build();
    }
}
