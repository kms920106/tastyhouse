package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductRepresentativeRequestState(
    Long id,
    Long productId,
    Long shopId,
    String status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
