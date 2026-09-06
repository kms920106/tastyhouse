package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;

public record ShopRequestImageChangeDetailResult(
    ShopImageType imageType,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {
}
