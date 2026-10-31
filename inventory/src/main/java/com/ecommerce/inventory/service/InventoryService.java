
package com.ecommerce.inventory.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.model.Reservation;
import com.ecommerce.inventory.repo.InventoryRepository;
import com.ecommerce.inventory.repo.ReservationRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    public record ItemRequest(Long productId, int quantity) {}

    public record StockRequest(Long quantity) {}

    public record ReserveRequest(
        Long orderId,
        List<ItemRequest> items
    ) {}

    public record ReservationResponse(
        Long id,
        Long orderId,
        String status
    ) {}

    @Transactional(readOnly = true)
    public Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Inventory not found for product " + productId));
    }

    @Transactional
    public Inventory updateInventory(
            Long productId,
            StockRequest request) {
        if (productId == null ||
            request == null ||
            request.quantity() == null ||
            request.quantity() < 0) {
            throw new IllegalArgumentException(
                "Product ID and non-negative quantity are required");
        }

        Inventory inventory = inventoryRepository
            .findByProductIdForUpdate(productId)
            .orElseGet(() -> {
                Inventory newInventory = new Inventory();
                newInventory.setProductId(productId);
                newInventory.setReserved(0L);
                return newInventory;
            });

        inventory.setQuantity(request.quantity());
        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        return reservationRepository.findByOrderId(orderId).stream()
            .map(reservation -> new ReservationResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getStatus()
            ))
            .toList();
    }

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {

        if (request.orderId() == null ||
            request.items() == null ||
            request.items().isEmpty()) {
            throw new IllegalArgumentException(
                "Order ID and items are required");
        }

        // Validate all input before making any stock changes.
        for (ItemRequest item : request.items()) {
            if (item.productId() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException(
                    "Invalid product or quantity");
            }
        }

        List<Reservation> created = new ArrayList<>();

        // The transaction ensures partial stock updates are rolled back
        // if any item cannot be reserved.
        for (ItemRequest item : request.items()) {

            var existing = reservationRepository
                .findByOrderIdAndProductId(
                    request.orderId(), item.productId()
                );

            if (existing.isPresent()) {
                Reservation previous = existing.get();

                if (previous.getQuantity() == item.quantity() &&
                    "RESERVED".equals(previous.getStatus())) {
                    created.add(previous);
                    continue;
                }

                throw new IllegalStateException(
                    "Reservation already exists for this order and product");
            }

            Inventory inventory = inventoryRepository
                .findByProductIdForUpdate(item.productId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Inventory not found for product " +
                        item.productId()));

            if (inventory.getQuantity() < item.quantity()) {
                throw new IllegalStateException(
                    "Insufficient stock for product " + item.productId());
            }

            inventory.setQuantity(
                inventory.getQuantity() - item.quantity());

            inventory.setReserved(
                inventory.getReserved() + item.quantity());

            inventoryRepository.save(inventory);

            Reservation reservation = new Reservation();
            reservation.setOrderId(request.orderId());
            reservation.setProductId(item.productId());
            reservation.setQuantity(item.quantity());
            reservation.setStatus("RESERVED");

            created.add(reservationRepository.save(reservation));
        }

        Reservation first = created.get(0);

        return new ReservationResponse(
            first.getId(),
            first.getOrderId(),
            first.getStatus()
        );
    }

    @Transactional
    public ReservationResponse release(Long reservationId) {

        Reservation reservation = reservationRepository
            .findById(reservationId)
            .orElseThrow(() ->
                new IllegalArgumentException("Reservation not found"));

        // A repeated release must not add stock twice.
        if ("RELEASED".equals(reservation.getStatus())) {
            return new ReservationResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getStatus()
            );
        }

        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new IllegalStateException(
                "Only RESERVED stock can be released");
        }

        Inventory inventory = inventoryRepository
            .findByProductIdForUpdate(reservation.getProductId())
            .orElseThrow(() ->
                new IllegalStateException("Inventory record not found"));

        inventory.setQuantity(
            inventory.getQuantity() + reservation.getQuantity());

        inventory.setReserved(
            inventory.getReserved() - reservation.getQuantity());

        inventoryRepository.save(inventory);

        reservation.setStatus("RELEASED");
        reservationRepository.save(reservation);

        return new ReservationResponse(
            reservation.getId(),
            reservation.getOrderId(),
            reservation.getStatus()
        );
    }
}
