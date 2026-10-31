package com.ecommerce.user.repo;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.model.Address;

@Repository 
public interface AddressRepo extends JpaRepository<Address,Long> {
    List<Address> findByUserId(Long userId);
}
