package io.namson.targetapi.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String name,
        String description,
        BigDecimal unitPrice,
        Integer quantity) {

}
