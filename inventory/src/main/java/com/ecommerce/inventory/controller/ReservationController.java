
package com.ecommerce.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.inventory.service.InventoryService.ReserveRequest;
import com.ecommerce.inventory.service.InventoryService.ReservationResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final InventoryService inventoryService;

    public ReservationController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(
            @RequestBody ReserveRequest request) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/{id}/release")
    public ReservationResponse release(
            @PathVariable Long id) {
        return inventoryService.release(id);
    }

    @GetMapping("/order/{orderId}")
    public List<ReservationResponse> getByOrderId(
            @PathVariable Long orderId) {
        return inventoryService.getReservationsByOrderId(orderId);
    }
}
