package io.namson.targetapi.dto;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        UUID uuid,
        UUID userId,
        String name,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        List<OrderItemResponse> items) {

}