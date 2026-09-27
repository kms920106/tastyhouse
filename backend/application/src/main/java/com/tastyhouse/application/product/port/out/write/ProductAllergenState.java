package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductAllergenState(
    Long id,
    Long productId,
    String allergenType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
