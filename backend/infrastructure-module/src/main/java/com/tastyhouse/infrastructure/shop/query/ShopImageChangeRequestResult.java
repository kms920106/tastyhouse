package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;

public record ShopImageChangeRequestResult(
    Long id,
    Long shopId,
    ShopImageType imageType,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {

}
