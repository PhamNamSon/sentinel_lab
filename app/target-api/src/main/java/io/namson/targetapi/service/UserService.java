package io.namson.targetapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.namson.targetapi.dto.CreateUserRequest;
import io.namson.targetapi.dto.UserResponse;
import io.namson.targetapi.dto.UserOrderResponse;
import io.namson.targetapi.entity.User;
import io.namson.targetapi.entity.Order;
import io.namson.targetapi.exception.ResourceNotFoundException;
import io.namson.targetapi.repository.OrderRepository;
import io.namson.targetapi.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public UserService(UserRepository userRepository, OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.orderRepository = orderRepository;

    }

    public UserResponse createUser(CreateUserRequest request) {

        User user = new User(
                request.name(),
                request.email());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUuid(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt());

    }

    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return new UserResponse(
                user.getUuid(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt());

    }

    public List<UserOrderResponse> getUserOrders(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        List<Order> orders = orderRepository.findByUser(user);
        List<UserOrderResponse> userOrdersResponse = new ArrayList<>();

        for (Order order : orders) {
            UserOrderResponse response = new UserOrderResponse(
                    order.getUuid(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    order.getCreatedAt());

            userOrdersResponse.add(response);
        }

        return userOrdersResponse;

    }

}
