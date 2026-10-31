package com.ecommerce.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.user.model.Address;
import com.ecommerce.user.service.AddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class addressController {

    private final AddressService addressService;

    // Create address
    @PostMapping("/user/{userId}")
    public ResponseEntity<Address> createAddress(
            @PathVariable Long userId,
            @RequestBody Address address,
            @AuthenticationPrincipal Jwt jwt) {

        requireOwnUser(userId, jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(userId, address));
    }

    // Get all addresses
    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
            addressService.getAddressesByUserId(userId(jwt)));
    }

    // Get address by ID
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
            addressService.getAddressById(id, userId(jwt)));
    }

    // Get addresses by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getAddressesByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {

        requireOwnUser(userId, jwt);
        return ResponseEntity.ok(
                addressService.getAddressesByUserId(userId));
    }

    // Update address
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @RequestBody Address address,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                addressService.updateAddress(id, userId(jwt), address));
    }

    // Delete address
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        addressService.deleteAddress(id, userId(jwt));
        return ResponseEntity.noContent().build();
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