package com.ecommerce.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.inventory.service.InventoryService.StockRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InventoryControllerTest {

    @Test
    void postUpdatesInventory() throws Exception {
        InventoryService inventoryService = mock(InventoryService.class);
        Inventory inventory = new Inventory();
        inventory.setProductId(42L);
        inventory.setQuantity(15L);
        inventory.setReserved(0L);

        when(inventoryService.updateInventory(
                eq(42L), any(StockRequest.class)))
                .thenReturn(inventory);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new InventoryController(inventoryService))
                .build();

        mockMvc.perform(post("/inventory/42")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(42))
                .andExpect(jsonPath("$.quantity").value(15));

        verify(inventoryService).updateInventory(
                eq(42L), any(StockRequest.class));
    }
}
