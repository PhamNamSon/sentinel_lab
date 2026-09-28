package io.namson.targetapi.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID productId, int requested, int available) {
        super("Insufficient stock for product with id: " + productId + ". Requested: " + requested + ", Available: "
                + available);
    }

}
