package io.namson.targetapi.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.namson.targetapi.enums.OrderStatus;

public record UserOrderResponse(
                UUID orderId,
                BigDecimal totalPrice,
                OrderStatus status,
                LocalDateTime createdAt) {

}
