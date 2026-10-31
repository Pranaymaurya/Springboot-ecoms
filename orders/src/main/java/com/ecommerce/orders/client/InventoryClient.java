
package com.ecommerce.orders.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "INVENTORY")
public interface InventoryClient {

    @PostMapping("/reservations")
    ReservationResponse reserve(
        @RequestHeader("Authorization") String authorization,
        @RequestBody ReservationRequest request
    );

    @PostMapping("/reservations/{id}/release")
    void release(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("id") Long id
    );

    record ReservationItem(
        Long productId,
        int quantity
    ) {}

    record ReservationRequest(
        Long orderId,
        List<ReservationItem> items
    ) {}

    record ReservationResponse(
        Long id,
        Long orderId,
        String status
    ) {}
}
