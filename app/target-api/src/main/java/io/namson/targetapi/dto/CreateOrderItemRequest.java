package io.namson.targetapi.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull(message = "Product ID is required") UUID productId,
        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity cannot be less than 1") Integer quantity) {

}
