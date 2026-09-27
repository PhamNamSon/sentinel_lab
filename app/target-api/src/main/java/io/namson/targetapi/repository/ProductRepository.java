package io.namson.targetapi.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.namson.targetapi.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable);

}
