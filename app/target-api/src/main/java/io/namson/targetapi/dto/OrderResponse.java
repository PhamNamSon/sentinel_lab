package io.namson.targetapi.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        UUID uuid,
        UUID userId,
        BigDecimal totalPrice,
        LocalDateTime createdAt) {

}