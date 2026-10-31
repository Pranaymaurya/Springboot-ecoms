package com.ecommerce.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.ecommerce.user.model.User;

@Repository 
public interface UserRepo extends JpaRepository<User,Long> {

    Optional<User> findByEmailIgnoreCase(String email);
}
