
package com.ecommerce.orders.dto;

import java.util.List;

public record CreateOrderRequest(
    List<OrderItemRequest> items
) {}
