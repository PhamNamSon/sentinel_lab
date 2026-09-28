package io.namson.targetapi.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import io.namson.targetapi.dto.CreateOrderRequest;
import io.namson.targetapi.dto.OrderResponse;
import io.namson.targetapi.dto.ProductResponse;
import io.namson.targetapi.service.OrderService;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {

        this.orderService = orderService;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(request);

    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable UUID id) {

        return orderService.getOrderById(id);

    }
}
