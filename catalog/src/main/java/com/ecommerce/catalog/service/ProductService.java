package com.ecommerce.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repo.ProductRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepository;

    // Create product
    public Product createProduct(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Product not found with id: " + id));
    }

    // Update product
    public Product updateProduct(Long id, Product product) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Product not found with id: " + id));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());

        return productRepository.save(existingProduct);
    }

    // Delete product
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Product not found with id: " + id));

        productRepository.delete(product);
    }
}