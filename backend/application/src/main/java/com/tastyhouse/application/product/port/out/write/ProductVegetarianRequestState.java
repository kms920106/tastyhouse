package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductVegetarianRequestState(
    Long id,
    Long productId,
    String vegetarianType,
    String ingredients,
    String description,
    String status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
