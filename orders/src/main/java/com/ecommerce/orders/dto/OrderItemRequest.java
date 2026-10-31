
package com.ecommerce.orders.dto;

public record OrderItemRequest(
    Long productId,
    int quantity,
    Double price
) {}
