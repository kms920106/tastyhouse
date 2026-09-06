package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ProductImageChangeRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String productName,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {
}
