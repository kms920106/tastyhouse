package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ShopRequestImageChangeDetailResult(
    String imageType,
    String imageTypeDescription,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {
}
