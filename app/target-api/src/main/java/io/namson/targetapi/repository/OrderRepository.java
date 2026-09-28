package io.namson.targetapi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.namson.targetapi.entity.Order;
import io.namson.targetapi.entity.User;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUser(User user);

}
