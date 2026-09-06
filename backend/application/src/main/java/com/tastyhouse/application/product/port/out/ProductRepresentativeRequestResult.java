package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ProductRepresentativeRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String shopName,
    String productName,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {
}
