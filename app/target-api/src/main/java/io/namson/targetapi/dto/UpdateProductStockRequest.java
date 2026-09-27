package io.namson.targetapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStockRequest(
        @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") Integer stock) {

}
