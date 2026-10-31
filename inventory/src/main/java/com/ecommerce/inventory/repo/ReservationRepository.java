package com.ecommerce.inventory.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.inventory.model.Reservation;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByOrderIdAndProductId(
        Long orderId, Long productId
    );

    List<Reservation> findByOrderId(Long orderId);
}