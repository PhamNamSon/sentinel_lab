package io.namson.targetapi.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "User ID is required") UUID userId,
        @NotNull(message = "Products list is required") List<@Valid CreateOrderItemRequest> products) {

}
