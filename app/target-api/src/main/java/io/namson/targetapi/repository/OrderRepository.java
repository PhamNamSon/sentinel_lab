package io.namson.targetapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.namson.targetapi.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

}
