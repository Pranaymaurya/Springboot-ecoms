package com.ecommerce.inventory.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.inventory.service.InventoryService.StockRequest;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public Inventory getInventory(@PathVariable Long productId) {
        return inventoryService.getInventory(productId);
    }

    @RequestMapping(
            path = "/{productId}",
            method = {RequestMethod.PUT, RequestMethod.POST})
    public Inventory updateInventory(
            @PathVariable Long productId,
            @RequestBody StockRequest request) {
        return inventoryService.updateInventory(productId, request);
    }
}
