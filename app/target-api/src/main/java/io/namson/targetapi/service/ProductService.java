package io.namson.targetapi.service;

import org.springframework.stereotype.Service;

import io.namson.targetapi.dto.CreateProductRequest;
import io.namson.targetapi.entity.Product;
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

}
