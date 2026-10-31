package com.ecommerce.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repo.AddressRepo;
import com.ecommerce.user.repo.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepo addressRepository;
    private final UserRepo userRepository;

    // Create address
    public Address createAddress(Long userId, Address address) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + userId));

        address.setId(null);
        address.setUser(user);

        return addressRepository.save(address);
    }

    // Get all addresses
    @Transactional(readOnly = true)
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    // Get address by ID
    @Transactional(readOnly = true)
    public Address getAddressById(Long id, Long userId) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Address not found with id: " + id));
        if (!address.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException(
                "Address not found with id: " + id);
        }
        return address;
    }

    // Get addresses by user ID
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(
                    "User not found with id: " + userId);
        }

        return addressRepository.findByUserId(userId);
    }

    // Update address
    public Address updateAddress(Long id, Long userId, Address address) {

        Address existingAddress = getAddressById(id, userId);

        existingAddress.setStreet(address.getStreet());
        existingAddress.setLocality(address.getLocality());
        existingAddress.setPincode(address.getPincode());

        return addressRepository.save(existingAddress);
    }

    // Delete address
    public void deleteAddress(Long id, Long userId) {

        Address address = getAddressById(id, userId);

        addressRepository.delete(address);
    }
}