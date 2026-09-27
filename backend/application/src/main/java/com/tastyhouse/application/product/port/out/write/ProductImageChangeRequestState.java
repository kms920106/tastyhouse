package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductImageChangeRequestState(
    Long id,
    Long productId,
    Long imageFileId,
    String status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
