package io.namson.targetapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import io.namson.targetapi.dto.CreateOrderItemRequest;
import io.namson.targetapi.dto.CreateOrderRequest;
import io.namson.targetapi.dto.OrderItemResponse;
import io.namson.targetapi.dto.OrderResponse;
import io.namson.targetapi.dto.ProductResponse;
import io.namson.targetapi.entity.Order;
import io.namson.targetapi.entity.OrderItem;
import io.namson.targetapi.entity.Product;
import io.namson.targetapi.entity.User;
import io.namson.targetapi.exception.InsufficientStockException;
import io.namson.targetapi.exception.ResourceNotFoundException;
import io.namson.targetapi.repository.OrderRepository;
import io.namson.targetapi.repository.ProductRepository;
import io.namson.targetapi.repository.UserRepository;
import io.namson.targetapi.repository.OrderItemRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        List<Product> products = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        for (CreateOrderItemRequest item : request.products()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));

            if (product.getStock() < item.quantity()) {
                throw new InsufficientStockException(product.getUuid(), item.quantity(), product.getStock());
            }

            products.add(product);
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.quantity()));
            totalPrice = totalPrice.add(itemTotal);

        }

        Order savedOrder = orderRepository.save(new Order(user, totalPrice));
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();

        for (int i = 0; i < request.products().size(); i++) {
            CreateOrderItemRequest item = request.products().get(i);
            Product product = products.get(i);

            product.setStock(product.getStock() - item.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(savedOrder, product, item.quantity(), product.getPrice());
            orderItemRepository.save(orderItem);

            orderItemResponses.add(new OrderItemResponse(
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    item.quantity()));
        }

        return new OrderResponse(
                savedOrder.getUuid(),
                savedOrder.getUser().getUuid(),
                savedOrder.getUser().getName(),
                savedOrder.getTotalPrice(),
                savedOrder.getCreatedAt(),
                orderItemResponses);

    }

    public OrderResponse getOrderById(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            orderItemResponses.add(new OrderItemResponse(
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    orderItem.getQuantity()));
        }

        return new OrderResponse(
                order.getUuid(),
                order.getUser().getUuid(),
                order.getUser().getName(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                orderItemResponses);

    }

}
