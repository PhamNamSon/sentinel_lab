package io.namson.targetapi.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import io.namson.targetapi.dto.CreateProductRequest;
import io.namson.targetapi.dto.ProductResponse;
import io.namson.targetapi.dto.UpdateProductStockRequest;
import io.namson.targetapi.service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {

        this.productService = productService;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {

        return productService.createProduct(request);

    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {

        return productService.getProductById(id);

    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return productService.searchProducts(name, page, size);

    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateProductStock(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductStockRequest request) {

        return productService.updateProductStock(id, request.stock());

    }

}
