package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ShopMenuCollectionImageResult(
    Long id,
    String imageUrl,
    Integer sort,
    ApprovalStatus status,
    String rejectReason
) {
}
