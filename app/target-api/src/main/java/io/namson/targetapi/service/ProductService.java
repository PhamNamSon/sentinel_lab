package io.namson.targetapi.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.namson.targetapi.dto.CreateProductRequest;
import io.namson.targetapi.entity.Product;
import io.namson.targetapi.exception.ResourceNotFoundException;
import io.namson.targetapi.repository.ProductRepository;
import io.namson.targetapi.dto.ProductResponse;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock());

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
                savedProduct.getUuid(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getStock(),
                savedProduct.getCreatedAt());
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        return new ProductResponse(
                product.getUuid(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt());
    }

    public Page<ProductResponse> searchProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);

        return products.map(product -> new ProductResponse(
                product.getUuid(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt()));
    }

    public ProductResponse updateProductStock(UUID id, int stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setStock(stock);
        Product updatedProduct = productRepository.save(product);

        return new ProductResponse(
                updatedProduct.getUuid(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getStock(),
                updatedProduct.getCreatedAt());
    }
}
