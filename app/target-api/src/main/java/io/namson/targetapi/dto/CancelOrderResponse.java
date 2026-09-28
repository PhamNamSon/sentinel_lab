package io.namson.targetapi.dto;

import java.util.UUID;

import io.namson.targetapi.enums.OrderStatus;

public record CancelOrderResponse(
        UUID orderId,
        OrderStatus status,
        String message) {

}