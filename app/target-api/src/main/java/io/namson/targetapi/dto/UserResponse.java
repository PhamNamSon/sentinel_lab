package io.namson.targetapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
                UUID id,
                String name,
                String email,
                LocalDateTime createdAt) {

}
