package com.ecommerce.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.catalog.model.Product;

public interface ProductRepo extends JpaRepository<Product,Long> {

    
}
