package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ShopMenuCollectionImageRequestResult(
    Long id,
    Long shopId,
    String shopName,
    String imageUrl,
    Integer sort,
    ApprovalStatus status,
    String rejectReason
) {
}
